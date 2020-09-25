from django.db import models


class Hello(models.Model):
    f_name=models.CharField(max_length=30)
    l_name=models.CharField(max_length=30)

# Create your models here.
