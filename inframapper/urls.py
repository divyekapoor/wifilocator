from django.conf.urls.defaults import *
from wifilocator.inframapper import views

urlpatterns = patterns('',
    (r'^externalscan/', views.external_scan),
    (r'^scan/', views.scan),
    (r'^ping/', views.ping),
    (r'^deregister/ap', views.deregisterAP),
    (r'^register/ap', views.registerAP),
    (r'^nearest/ap', views.nearestAP),
    (r'^arp/', views.arp),
    (r'^rarp/', views.rarp),
    (r'^locate/', views.locate),
    (r'^$', views.home),
)

