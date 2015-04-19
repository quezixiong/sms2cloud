import time
from datetime import datetime
from django.db import transaction
from lxml import etree
from django.http import HttpResponse
from django.shortcuts import render
from django.views.decorators.csrf import csrf_exempt
from django.views.decorators.http import require_http_methods, require_POST, require_GET
from rest_framework.parsers import JSONParser
from rest_framework.renderers import JSONRenderer
from sms2cloud.forms import *
from sms2cloud.models import *
from sms2cloud.api import ErrCode
from hashlib import sha1


class JSONResponse(HttpResponse):
    """
    An HttpResponse that renders its content into JSON.
    """
    def __init__(self, data, **kwargs):
        content = JSONRenderer().render(data)
        kwargs['content_type'] = 'application/json'
        super(JSONResponse, self).__init__(content, **kwargs)


@require_GET
def get_bind(request):
    iccid = request.GET.get("iccid")
    if not iccid:
        data = {"errcode": ErrCode.ICCID_ALREADY_BIND, "errmsg": "iccid not valid"}
        return JSONResponse(data=data, status=200)
    if Phone.objects.filter("iccid").exists():
        phone = Phone.objects.get(iccid=iccid)
        if phone.is_bind():
            data = {"errmsg": "iccid already bind", "errcode": ErrCode.ICCID_ALREADY_BIND}
            return JSONResponse(data=data, status=200)
    else:
        phone = Phone.objects.create(identifier=Phone.get_identifier(), iccid=iccid)
    data = phone.get_bind_info()
    data["errcode"] = ErrCode.OK
    return JSONResponse(data=data, status=200)



@require_GET
def get_bind_status(request):
    identifier = request.GET.get('identifier')
    data = JSONParser().parse(request)
    iccid = data.get("iccid")
    if not Phone.check_auth(identifier, iccid):
        data = {"errcode": ErrCode.NOT_AUTHORIZED, "errmsg": "not authorized"}
        return JSONResponse(data=data, status=200)
    if not identifier or not Phone.objects.filter(identifier=identifier).exists():
        data = {"errcode": ErrCode.IDENTIFIER_NOT_VALID, "errmsg": "identifier not valid"}
        return JSONResponse(data=data, status=200)
    else:
        phone = Phone.objects.get(identifier=identifier)
        data = {"is_bind": phone.is_bind(), "errcode": ErrCode.OK}
        return JSONResponse(data=data, status=200)


@csrf_exempt
@require_POST
def bind(request):
    data = JSONParser().parse(request)
    number = data.get("number")
    sim_serial = data.get("serialNumber")
    if User.objects.filter(number=number).exists():
        uc = User.objects.get(number=number)
        uc.sim_serial = sim_serial
        uc.save()
    else:
        User.objects.create(number=number, sim_serial=sim_serial)
    data = {"errcode": 0, "errmsg": "success"}
    return JSONResponse(data=data, status=200)


@csrf_exempt
@require_POST
def message(request):
    data = JSONParser().parse(request)
    number = data.get("number")
    sim_serial = data.get("serialNumber")
    message = data.get("message")
    if auth_number(number, sim_serial):
        uc = User.objects.get(number=number)
        Message.objects.create(message=message, credential=uc)
        data = {"errcode": 0, "errmsg": "success"}
        return JSONResponse(data=data, status=200)
    else:
        return JSONResponse(data={"errcode": -1}, status=200)


def auth(number, sim_serial):
    if User.objects.filter(number=number).exists():
        uc = User.objects.get(number=number)
        return uc.sim_serial == sim_serial
    else:
        return False


@csrf_exempt
@require_http_methods(['POST', 'GET'])
def subscribe(request):
    if request.method == "GET":
        code = request.GET.get("code")
        if not code:
            return HttpResponse("code is needed")
        return render(request, "sms2cloud/subscribe.html", {"code": code})
    elif request.method == "POST":
        form = NumberForm(request.POST)
        if form.is_valid():
            number = form.cleaned_data['number']
            code = form.cleaned_data['code']
            open_id = WechatCredential.get_open_id_by_code(code)
            if User.objects.filter(number=number).exists():
                uc = User.objects.get(number=number)
                uc.open_id = open_id
                uc.save()
            else:
                User.objects.create(number=number, open_id=open_id)
            return HttpResponse("success", status=200)
        return HttpResponse("post data is valid", status=400)


def auth_number(number, sim_serial):
    if User.objects.filter(number=number).exists():
        uc = User.objects.get(number=number)
        return uc.sim_serial == sim_serial
    else:
        return False



def check_signature(request, verify_token):
    signature = request.GET.get('signature')
    timestamp = request.GET.get('timestamp')
    nonce = request.GET.get('nonce')
    tmp_arr = sorted([verify_token, timestamp, nonce])
    tmp_str = ''.join(tmp_arr)
    tmp_str = sha1(tmp_str.encode('ascii')).hexdigest()
    return tmp_str == signature


def get_unread_message(uc):
    result = ""
    messages = Message.objects.filter(credential=uc, is_read=False)
    with transaction.atomic():
        for message in messages:
            result += message.content + "\n"
            message.is_read = True
            message.save()
    return result


@csrf_exempt
def server_handler(request):
    verify_token = WechatCredential.server_token
    if request.method == "GET":
        if check_signature(request, verify_token):
            return HttpResponse(request.GET.get('echostr'))
    elif request.method == "POST":
        if not check_signature(request, verify_token):
            return HttpResponse(status=400)
        parser = etree.XMLParser(strip_cdata=False)
        root = etree.XML(request.body, parser)
        msg_type = root.find("MsgType").text
        event_key = root.find("EventKey").text
        if msg_type == 'event' and event_key == GET_UNREAD_MESSAGE_KEY:
            open_id = root.find("FromUserName").text
            wechat_id = root.find("ToUserName").text
            if not User.objects.filter(open_id=open_id).exists():
                pass
            else:
                uc = User.objects.get(open_id=open_id)
                msg = get_unread_message(uc)
                # Construct a Response xml
                root = etree.Element('xml')
                to_user_name = etree.Element('ToUserName')
                to_user_name.text = etree.CDATA(open_id)
                root.append(to_user_name)
                from_user_name = etree.Element('FromUserName')
                from_user_name.text = etree.CDATA(wechat_id)
                root.append(from_user_name)
                create_time = etree.Element('CreateTime')
                create_time.text = etree.CDATA(str(int(time.mktime(datetime.now().timetuple()))))
                root.append(create_time)
                msg_type = etree.Element('MsgType')
                msg_type.text = etree.CDATA('text')
                root.append(msg_type)
                content = etree.Element('Content')
                content.text = etree.CDATA(msg)
                root.append(content)
            return HttpResponse(etree.tostring(root, encoding="utf-8"), content_type='text/xml')
        else:
            return HttpResponse(status=200)

