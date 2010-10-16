from django.conf.urls.defaults import *
from django.contrib import admin
admin.autodiscover()

from django.contrib import auth

urlpatterns = patterns('',
    (r'^admin/doc/', include('django.contrib.admindocs.urls')),
    (r'^admin/', include(admin.site.urls)),
    (r'^accounts/', include('django.contrib.auth.urls')),
    (r'', include('inframapper.urls')),
)
