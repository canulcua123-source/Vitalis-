// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'auth_models.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AuthRequestModel _$AuthRequestModelFromJson(Map<String, dynamic> json) =>
    AuthRequestModel(
      email: json['email'] as String,
      password: json['password'] as String,
    );

Map<String, dynamic> _$AuthRequestModelToJson(AuthRequestModel instance) =>
    <String, dynamic>{'email': instance.email, 'password': instance.password};

AuthResponseModel _$AuthResponseModelFromJson(Map<String, dynamic> json) =>
    AuthResponseModel(
      token: json['token'] as String,
      email: json['email'] as String,
      role: json['role'] as String,
    );

Map<String, dynamic> _$AuthResponseModelToJson(AuthResponseModel instance) =>
    <String, dynamic>{
      'token': instance.token,
      'email': instance.email,
      'role': instance.role,
    };

RegisterRequestModel _$RegisterRequestModelFromJson(
  Map<String, dynamic> json,
) => RegisterRequestModel(
  firstName: json['firstName'] as String,
  lastName: json['lastName'] as String,
  email: json['email'] as String,
  password: json['password'] as String,
  phone: json['phone'] as String?,
  birthDate: json['birthDate'] as String?,
  medicalHistory: json['medicalHistory'] as String?,
);

Map<String, dynamic> _$RegisterRequestModelToJson(
  RegisterRequestModel instance,
) => <String, dynamic>{
  'firstName': instance.firstName,
  'lastName': instance.lastName,
  'email': instance.email,
  'password': instance.password,
  'phone': instance.phone,
  'birthDate': instance.birthDate,
  'medicalHistory': instance.medicalHistory,
};

RegisterDoctorRequestModel _$RegisterDoctorRequestModelFromJson(
  Map<String, dynamic> json,
) => RegisterDoctorRequestModel(
  firstName: json['firstName'] as String,
  lastName: json['lastName'] as String,
  email: json['email'] as String,
  password: json['password'] as String,
  specialtyName: json['specialtyName'] as String,
  experienceYears: (json['experienceYears'] as num).toInt(),
  consultationPrice: (json['consultationPrice'] as num).toDouble(),
  slotDurationMinutes: (json['slotDurationMinutes'] as num?)?.toInt(),
  bufferTimeMinutes: (json['bufferTimeMinutes'] as num?)?.toInt(),
  location: json['location'] as String?,
  bio: json['bio'] as String?,
  photoUrl: json['photoUrl'] as String?,
);

Map<String, dynamic> _$RegisterDoctorRequestModelToJson(
  RegisterDoctorRequestModel instance,
) => <String, dynamic>{
  'firstName': instance.firstName,
  'lastName': instance.lastName,
  'email': instance.email,
  'password': instance.password,
  'specialtyName': instance.specialtyName,
  'experienceYears': instance.experienceYears,
  'consultationPrice': instance.consultationPrice,
  'slotDurationMinutes': instance.slotDurationMinutes,
  'bufferTimeMinutes': instance.bufferTimeMinutes,
  'location': instance.location,
  'bio': instance.bio,
  'photoUrl': instance.photoUrl,
};
