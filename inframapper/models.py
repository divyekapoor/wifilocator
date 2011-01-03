from django.db import models

class Location(models.Model):
    latitude = models.FloatField()
    longitude = models.FloatField()

    def __unicode__(self):
        return "(%f N, %f E)" % (self.latitude, self.longitude)


class FloorPlan(models.Model):
    image = models.ImageField(upload_to="uploads", height_field='height', width_field='width', max_length=1000)
    height = models.IntegerField(default=0, editable=False)
    width = models.IntegerField(default=0, editable=False)
    NElocation = models.ForeignKey(Location, related_name="NElocation") # The bounding box of the floor plan in terms of GPS coordinates
    SWlocation = models.ForeignKey(Location, related_name="SWlocation") #    - NE and SW corners of the bounding box
    description = models.CharField(max_length=1000)
    scale = models.FloatField()

    def __unicode__(self):
        return self.description

class IndoorLocation(models.Model):
    floor_plan = models.ForeignKey(FloorPlan)
    xoffset = models.IntegerField()         # The offsets of the position from the NW of the floor plan
    yoffset = models.IntegerField()
    description = models.CharField(max_length=1000, default="")

    def __unicode__(self):
        if self.description != "":
            return self.description
        else:
            return "%s: %d %d" % (self.floor_plan, self.xoffset, self.yoffset)


class AccessPoint(models.Model):
    eth_address = models.CharField(max_length=18)
    location = models.ForeignKey(IndoorLocation)
    essid = models.CharField(max_length=1024)

    def __unicode__(self):
        return "%s - %s" % (self.ip_address, self.location)


class Sample(models.Model):
    ap = models.ForeignKey(AccessPoint)
    rssi = models.IntegerField() # Value in db
    quality = models.IntegerField()
    max_quality = models.IntegerField()
    channel = models.IntegerField()
    freq = models.FloatField()
    sample_time = models.DateTimeField(auto_now=True)

    def __unicode__(self):
        return "%s: %d dbm %s" % (self.sample_time, self.rssi, self.ap)

