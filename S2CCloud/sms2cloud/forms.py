from django import forms
from sms2cloud.models import *


# class BindForm(forms.ModelForm):
#     class Meta:
#         model = User
#         fields = ['number', 'sim_serial']


class MessageForm(forms.Form):
    content = forms.CharField(max_length=200, required=True)
    number = forms.CharField(max_length=11, required=True)
    sim_serial = forms.CharField(max_length=100, required=True)


class NumberForm(forms.Form):
    number = forms.CharField(max_length=11, required=True)
    code = forms.CharField(required=True)

