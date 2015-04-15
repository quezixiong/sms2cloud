from django.conf.urls import patterns, url, include
from sms2cloud import views

urlpatterns = patterns('',
                       url(r'^', views.server_handler, name="server_handler"),
                       url(r'^user/bind', views.bind, name="bind"),
                       url(r'^user/message', views.message, name="message"),
                       url(r'^subscribe', views.subscribe, name="subscribe"),
                       )