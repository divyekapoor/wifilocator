from django.http import HttpResponse
from django.contrib.auth.decorators import login_required
from django.shortcuts import render_to_response
from django.template import RequestContext
from subprocess import Popen, PIPE
import re
import simplejson as json
from urllib2 import urlopen
from pprint import pprint
from inframapper.models import *

ip_match_regex = re.compile("(\d{1,3})\.(\d{1,3})\.(\d{1,3})\.(\d{1,3})")

def error(request):
    """
        This function returns the error response web page in case of a malformed request
        to an HTTP API function.

        HTTP Status Code 400 conforms to a malformed request.
    """
    return HttpResponse("Your request was malformed.", status=400)


# Public API
def home(request):
    """ This function renders the home page from the home page html template  """
    return render_to_response("index.html", {}, context_instance=RequestContext(request))


def locate(request):
    """
        The mobile node supplies its ip address as well as the local wifi scanning data.
        The server then determines by accessing its database the likely location of the
        mobile node.

        This is the most critical code functionality.
    """
    return HttpResponse("Locate!")
    
def nearestAP(request):
    """
        Given an address + floor + wing, what is the nearest access point located physically?
    """
    return HttpResponse("Nearest AP!")


def geocode(request):
    """
        This function accesses the Google Maps API to reverse geocode a lat/long location to an 
        address. This function is required as a cross site access to the Google Maps API via
        javascript on the browser is subject to cross site scripting attacks.
    """
    if not hasattr(request.GET, 'lat') and not hasattr(request.GET, 'long'):
        error(request)
    
    lat = request.GET.get('lat', None)
    lon = request.GET.get('long', None)

    f = urlopen("http://maps.googleapis.com/maps/api/geocode/json?latlng=" + lat + "," + lon + "&sensor=false")
    return HttpResponse(f.read(), mimetype="application/json")


def maplist(request):
    """
        Return a list of maps present in the bounding box provided by the NorthEast and SouthWest
        corners of an estimation rectangle
    """
    NElat = request.GET.get('NElat', None)
    NElng = request.GET.get('NElng', None)
    SWlat = request.GET.get('SWlat', None)
    SWlng = request.GET.get('SWlng', None)

    if None in [NElat, NElng, SWlat, SWlng]:
        return error(request)

    floor_plans = FloorPlan.objects.filter(SWlocation__latitude__lte = NElat).filter(SWlocation__longitude__lte = NElng).filter(NElocation__latitude__gte = SWlat).filter(NElocation__longitude__gte = SWlng)
    result = { 'result' : 'OK','floorPlans' : [i.__dict__ for i in floor_plans]}
    for fp in result['floorPlans']:
        fp['image'] = fp['image'].url
    return HttpResponse(json.dumps(result), mimetype="application/json")
    

def mapshow(request):
    """
        Show the indoor maps along with the sample data for the access points.
    """
    if request.method != "POST":
        return error(request)
    
    assert False,request
    return render_to_response("indoor.html", {}, context_instance=RequestContext(request))

# Internal functions
def arp(request):
    """
        Perform an ARP request to resolve an IP to a MAC Address.
    """
    return HttpResponse("Arp!")

def rarp(request):
    """
        Due to lack of kernel support, perform a broadcast Ping, cache the 
        ARPs and try for an RARP. Else, report failure.
        Alternatively, use the net-tools package to support RARP via a daemon
    """
    return HttpResponse("Rarp!")

def scan(request):
    """
        Perform a local environment scan on the server itself and report the
        results.

        Provide an option for saving the results of the scan to the database.
    """
    return HttpResponse("Scan!")

@login_required
def external_scan(request):
    """
        A scan was performed by a trusted external entity. Save the scan results
        to the database.
    """
    return HttpResponse("External Scan!")


def ping(request):
    """
        The intention of this function is to allow a mobile node to see if another node
        is available via the infrastructure network while offloading the computation to
        the server.
    """
    # Get the IP Address to ping
    ip = request.GET.get("ip", None)
    if ip is None:
        return error(request)

    # Validate the IP Address 
    re_match_result = re.match(ip_match_regex, ip)
    if not re_match_result or len([x for x in re_match_result.groups() if int(x) > 255]) != 0:
        return error(request)

    # Perform a ping to the host
    ping_shell = Popen("ping -c 1 " + ip, shell=True, stdin=None, stdout=PIPE)
    ping_shell.wait()

    # Return the result as JSON
    return HttpResponse("%s" % json.dumps({"result" : not bool(ping_shell.returncode), "data" : {} }))

@login_required
def registerAP(request):
    return HttpResponse("Register AP!")

@login_required
def deregisterAP(request):
    return HttpResponse("Deregister AP!")

