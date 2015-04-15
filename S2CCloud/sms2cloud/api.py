"""
this file save all the api needed by wechat
"""

get_token_api = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid={app_id}&secret={app_secret}"

create_menu_api = "https://api.weixin.qq.com/cgi-bin/menu/create?access_token={access_token}"

subscribe_url =  "https://open.weixin.qq.com/connect/oauth2/authorize" \
                 "?appid={app_id}&redirect_uri={redirect_uri}&response_type=code&scope=snsapi_base#wechat_redirect"

get_open_id_api = "https://api.weixin.qq.com/sns/oauth2/access_token" \
                  "?appid={app_id}&secret={app_secret}&code={code}&grant_type=authorization_code"

# add_tmp_material_api = "https://api.weixin.qq.com/cgi-bin/media/upload?access_token={access_token}&type=image" #新增临时图片
#
# add_material_api = "http://api.weixin.qq.com/cgi-bin/material/add_material?access_token={access_token}&type=image" #新增永久图片
#
# news_preview_api = "https://api.weixin.qq.com/cgi-bin/message/mass/preview?access_token={access_token}"
#
# send_news_api = "https://api.weixin.qq.com/cgi-bin/message/mass/sendall?access_token={access_token}"
#
# upload_news_api = "https://api.weixin.qq.com/cgi-bin/media/uploadnews?access_token={access_token}" #上传临时图文素材
#
# add_news_api = "https://api.weixin.qq.com/cgi-bin/material/add_news?access_token={access_token}" #新增永久图文素材
#
# update_news_material_api = "https://api.weixin.qq.com/cgi-bin/material/update_news?access_token={access_token}" #修改永久图文素材
