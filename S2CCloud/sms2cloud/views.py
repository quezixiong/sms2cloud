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
    if Phone.objects.filter(iccid=iccid).exists():
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
def message(request):
    identifier = request.GET.get('identifier')
    data = JSONParser().parse(request)
    iccid = data.get("iccid")
    message = data.get("message")
    if not Phone.check_auth(identifier, iccid):
        data = {"errcode": ErrCode.NOT_AUTHORIZED, "errmsg": "not authorized"}
        return JSONResponse(data=data, status=200)
    if not identifier or not Phone.objects.filter(identifier=identifier).exists():
        data = {"errcode": ErrCode.IDENTIFIER_NOT_VALID, "errmsg": "identifier not valid"}
        return JSONResponse(data=data, status=200)
    else:
        phone = Phone.objects.get(identifier=identifier)
        Message.objects.create(content=message, owner=phone.owner)
        data = {"success": True, "errorcode": ErrCode.OK}
        return JSONResponse(data=data, status=200)


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
    server_token = WechatCredential.server_token
    if request.method == "GET":
        if check_signature(request, server_token):
            return HttpResponse(request.GET.get('echostr'))
    elif request.method == "POST":
        if not check_signature(request, server_token):
            return HttpResponse(status=400)
        # Get data xml tree
        parser = etree.XMLParser(strip_cdata=False)
        root = etree.XML(request.body, parser)

        from_user_name = root.find("FromUserName").text # Open_id
        to_user_name = root.find("ToUserName").text
        msg_type = root.find("MsgType").text

        # Response message
        response = ""

        # Identify bind event by ticket.
        ticket = root.find("Ticket").text
        if ticket:
            # if ticket not exists or ticket expired ,return wrong information.
            if not Phone.objects.filter(ticket=ticket).exists() or Phone.objects.get(ticket=ticket).is_ticket_expired:
                response = "绑定失败，二维码过期请重试"
            else:
                phone = Phone.objects.get(ticket=ticket)
                user = Phone.objects.get_or_create(open_id=from_user_name)
                phone.owner = user
                phone.save()
                response = "恭喜你，绑定成功"
        # Identify get unread msg event by msg_type.
        elif msg_type == "text":
            if not User.objects.filter(open_id=from_user_name).exists():
                response = "未绑定任何手机，请先绑定手机"
            else:
                user = User.objects.get(open_id=from_user_name)
                response = user.get_unread_msgs()
                if not response:
                    response = "没有未读信息"

        # Construct a Response xml
        ret_root = etree.Element('xml')
        send_to = etree.Element('ToUserName')
        send_to.text = etree.CDATA(from_user_name)
        ret_root.append(send_to)
        from_wechat = etree.Element('FromUserName')
        from_wechat.text = etree.CDATA(to_user_name)
        ret_root.append(from_wechat)
        create_time = etree.Element('CreateTime')
        create_time.text = etree.CDATA(str(int(time.mktime(datetime.now().timetuple()))))
        ret_root.append(create_time)
        msg_type = etree.Element('MsgType')
        msg_type.text = etree.CDATA('text')
        ret_root.append(msg_type)
        content = etree.Element('Content')
        content.text = etree.CDATA(response)
        ret_root.append(content)
        return HttpResponse(etree.tostring(ret_root, encoding="utf-8"), content_type='text/xml')
    else:
        return HttpResponse(status=200)

