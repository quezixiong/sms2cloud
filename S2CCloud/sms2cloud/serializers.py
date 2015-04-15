from rest_framework import serializers
from sms2cloud.exceptions import InvaildCrentialError
from sms2cloud.models import *


class BindSerializer(serializers.ModelSerializer):
    class Meta:
        model = UserCredential
        fields = ('number', 'sim_serial')
#
#
# class AndroidDataSerializer(serializers.ModelSerializer):
#     def create(self, validated_data):
#         with transaction.atomic():
#             instance = super(AndroidDataSerializer, self).create(validated_data)
#             if not instance.check_credential():
#                 raise InvaildCrentialError
#             instance.init_token()
#             return instance
#
#     def update(self, instance, validated_data):
#         app_id = validated_data.get('app_id')
#         app_secret = validated_data.get('app_secret')
#         with transaction.atomic():
#             instance.app_id = app_id
#             instance.app_secret = app_secret
#             if not instance.check_credential():
#                 raise InvaildCrentialError
#             return super(AndroidDataSerializer, self).update(instance, validated_data)
#
#     class Meta:
#         model = AndroidData
#         fields = ('app_id', 'app_secret', 'open_id', 'name')
