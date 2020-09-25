from django.shortcuts import render
from django.http import HttpResponse
from django.http import HttpResponseRedirect
from .models import Hello

def home(request):
    hello=Hello()
    hello.f_name1=request.POST.get('f_name')
    hello.l_name1=request.POST.get('l_name')
    hello.save()
    return render(request, 'App/Home.html')
# Create your views here.
def redirect(request):
    return render(request, 'App/Redirect.html')
