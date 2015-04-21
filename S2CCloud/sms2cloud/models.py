from datetime import timedelta
import json
import time
from datetime import datetime
import uuid
from sms2cloud.api import *
from django.utils import timezone
import requests
from sms2cloud.api import get_token_api
from sms2cloud.exceptions import *
from django.conf import settings
from django.db import models


class WechatCredential(models.Model):
    token_in_db = models.CharField(max_length=200, null=True, blank=False)
    token_update_time = models.DateTimeField(null=True)
    app_id = settings.APP_ID
    app_secret = settings.APP_SECRET
    server_token = settings.SERVER_TOKEN
    token_expire_in = 7200

    @property
    def token(self):
        if self.token_in_db and timezone.now() - self.token_update_time < timedelta(seconds=self.token_expire_in-60):
            return self.token_in_db
        else:
            r = requests.get(get_token_api.format(app_id=self.app_id, app_secret=self.app_secret))
            data = r.json()
            if r.status_code != 200 or data.get('errcode') == 40001:
                raise InvaildCrentialError
            self.token_in_db = data.get('access_token')
            self.token_update_time = timezone.now()
            self.save()
            return self.token_in_db

    @staticmethod
    def get_wc():
        if not WechatCredential.objects.all().exists():
            wc = WechatCredential.objects.create()
        else:
            wc = WechatCredential.objects.all()[0]
        return wc


class User(models.Model):
    open_id = models.CharField(max_length=100, null=True, blank=False, unique=True)

    def get_unread_msgs(self):
        ret = ""
        messages = self.messages.all()
        for msg in messages:
            if not msg.is_read:
                ret += "\n" + msg.content
                msg.is_read = True
                msg.save()
        return ret

    @property
    def is_bind(self):
        return bool(self.sim_serial)

    @property
    def is_subscribe(self):
        return bool(self.app_id)


class Phone(models.Model):
    ticket = models.CharField(max_length=100, null=True, blank=False)  # Used as scene_id.
    nickname = models.CharField(max_length=50, null=True, blank=True)
    identifier = models.CharField(max_length=100, null=True, blank=False)
    iccid = models.CharField(max_length=100, null=True, blank=False, unique=True)
    owner = models.ForeignKey(User, null=True, related_name="phones")

    ticket_update_time = models.DateTimeField(null=True)
    ticket_expire_in = 1800  # 1800 is max number allowed by wechat.

    def __init__(self, *args, **kwargs):
        self.wc = WechatCredential.get_wc()
        super(Phone, self).__init__(*args, **kwargs)

    def is_bind(self):
        return bool(self.owner)

    @staticmethod
    def get_identifier():
        return str(uuid.uuid4()) + str(int(time.mktime(datetime.now().timetuple())))

    @staticmethod
    def check_auth(identifier, iccid):
        if not Phone.objects.filter(identifier=identifier).exists():
            return False
        else:
            phone = Phone.objects.get(identifier=identifier)
            return phone.iccid == iccid

    @property
    def is_ticket_expired(self):
        return timezone.now() - self.ticket_update_time > timedelta(seconds=Phone.ticket_expire_in)

    def get_bind_info(self):
        url = create_qr_api.format(access_token=self.wc.token)
        data = {"expire_seconds": Phone.ticket_expire_in, "action_name": "QR_SCENE", "action_info": {"scene": {"scene_id": 123}}}
        r = requests.post(url, json=data)
        data = r.json()
        if r.status_code == 200:
            errcode = data.get("errcode")
            if errcode:
                raise GetQRFailed
            r_data = r.json()
            self.ticket = data.get("ticket")
            self.ticket_update_time = timezone.now()
            qr_url = data.get("url")
            return {"qr_url": qr_url, "identifier": self.identifier, "expire_in": Phone.ticket_expire_in}
        else:
            raise GetQRFailed


class Message(models.Model):
    content = models.CharField(max_length=200, null=False, blank=False)
    is_read = models.BooleanField(default=False)

    owner = models.ForeignKey(User, related_name="messages")

    class Meta:
        ordering = ['id']  # ordered by created_time ascending.

