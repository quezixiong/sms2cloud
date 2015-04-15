from datetime import timedelta
import json
import uuid
from sms2cloud.api import *
from django.db import models
from django.utils import timezone
import requests
from sms2cloud.api import get_token_api
from sms2cloud.exceptions import *
from django.conf import settings
from urllib.parse import quote_plus

GET_UNREAD_MESSAGE_KEY = "V1001_GET_UNREAD_MESSAGE"
BIND_PHONE_URL = "https://open.weixin.qq.com/connect/oauth2/authorize" \
                 "?appid={app_id}&redirect_uri={redirect_uri}&response_type=code&scope=snsapi_base&state=0" \
                 "#wechat_redirect"

class WechatCredential(models.Model):
    token_in_db = models.CharField(max_length=200, null=True, blank=False)
    token_update_time = models.DateTimeField(null=True)
    app_id = "wxcdab2d880b363449"
    app_secret = "a1177ac905855a4bc5cdf91a5d4f80e9"
    verify_token = "a1177ac905855a4bc5cdf91a5d4f80e9384027jdoru2"
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

    def init_menu(self):
        url = create_menu_api.format(access_token=self.token)
        redirect_uri = quote_plus("http://"+settings.DOMAIN+"/subscribe")
        data = {
            "button": [
                {
                    "type": "click",
                    "name": "收取未读信息",
                    "key": GET_UNREAD_MESSAGE_KEY,
                },
                {
                    "type": "view",
                    "name": "绑定手机号码",
                    "url": BIND_PHONE_URL.format(app_id=self.app_id, redirect_uri=redirect_uri)
                }]
        }
        data = json.dumps(data, ensure_ascii=False).encode('utf-8')
        r = requests.post(url, data=data)
        if r.status_code == 200:
            data = r.json()
            errcode = data.get('errcode', True)
            if errcode != 0:
                raise CreateMenuError
            return True

    @staticmethod
    def get_open_id_by_code(code):
        url = get_open_id_api.format(app_id=WechatCredential.app_id, app_secret=WechatCredential.app_secret, code=code)
        r = requests.get(url)
        data = r.json()
        if r.status_code == 200:
            open_id = data.get('openid')
            if open_id:
                return open_id
        raise GetOpenIDError



class UserCredential(models.Model):
    number = models.CharField(max_length=11, null=False, blank=False, unique=True)
    sim_serial = models.CharField(max_length=100, null=True, blank=False, unique=True)
    open_id = models.CharField(max_length=100, null=True, blank=False, unique=True)

    @property
    def is_bind(self):
        return bool(self.sim_serial)

    @property
    def is_subscribe(self):
        return bool(self.app_id)


class Message(models.Model):
    content = models.CharField(max_length=200, null=False, blank=False)
    is_read = models.BooleanField(default=False)

    credential = models.ForeignKey(UserCredential)

    class Meta:
        ordering = ['id']  # ordered by created_time ascending.

