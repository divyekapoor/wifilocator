MEDIA = '/home/divye/public_html/wifilocator/media'
IMAGES = 'inframapper/templates/images'
CSS = 'inframapper/templates/css'

all: media
	./manage.py syncdb
	./manage.py runserver 

media:
	cp -rf $(IMAGES) $(MEDIA)

.PHONY: all media

