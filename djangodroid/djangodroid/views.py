from django.http import HttpResponse
from django.views.decorators.csrf import csrf_exempt
from django.conf import settings
from django.shortcuts import render
import os

def index(request):
    return HttpResponse("Derp")

@csrf_exempt
def upload_file(request):
    if 'pic' in request.FILES:
        ret = handle_uploaded_file(request.FILES['pic'])
        return HttpResponse("Uploaded Image!" + "\n" + ret)
    return HttpResponse("No Image Received!")

def handle_uploaded_file(f):
    fname = os.path.join(os.getcwd(), 'media', f.name)
    destination = open(fname, 'wb+')
    for chunk in f.chunks(): 
        destination.write(chunk)
    destination.close()
    return fname

# if upload files are images
def view_images(request):
    images = [f for f in os.listdir(settings.MEDIA_ROOT)]
    print(images)
    return render(request, "djangodroid/index.html", {'images': images})
