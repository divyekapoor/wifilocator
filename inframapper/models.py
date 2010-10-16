from django.db import models

# Create your models here.
class GeoLocation(models.Model):
    latitude = models.FloatField()
    longitude = models.FloatField()
    address = models.CharField(max_length=10000)

    def __unicode__(self):
        return "(%d E, %d N) %s" % (self.gps_easting, self.gps_northing, self.address)

class IndoorLocation(models.Model):
    floor_plan = models.ImageField()


class AccessPoint(models.Model):
    ip_address = models.IPAddressField()
    eth_address = models.CharField(max_length=18)
    location = models.ForeignKey(Location)
    
    def __unicode__(self):
        return "%s - %s" % (self.ip_address, self.location)


class Sample(models.Model):
    ap = models.ForeignKey(AccessPoint)
    rssi = models.IntegerField() # Value in db
    sample_time = models.DateTimeField(auto_now=True)

    def __unicode__(self):
        return "%d dbm %s" % (self.rssi, self.ap)

