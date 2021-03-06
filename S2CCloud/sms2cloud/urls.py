from django.conf.urls import patterns, url, include
from sms2cloud import views

urlpatterns = patterns('',
                       url(r'^bind', views.get_bind),
                       url(r'^bind_status', views.get_bind_status),
                       url(r'^message', views.message, name="message"),
                       url(r'^server', views.server_handler, name="server_handler"),
                       )