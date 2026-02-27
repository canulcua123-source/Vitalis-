import 'package:json_annotation/json_annotation.dart';

part 'auth_models.g.dart';

@JsonSerializable()
class AuthRequestModel {
  final String email;
  final String password;

  AuthRequestModel({required this.email, required this.password});

  factory AuthRequestModel.fromJson(Map<String, dynamic> json) => _$AuthRequestModelFromJson(json);
  Map<String, dynamic> toJson() => _$AuthRequestModelToJson(this);
}

@JsonSerializable()
class AuthResponseModel {
  final String token;
  final String email;
  final String role;

  AuthResponseModel({required this.token, required this.email, required this.role});

  factory AuthResponseModel.fromJson(Map<String, dynamic> json) => _$AuthResponseModelFromJson(json);
  Map<String, dynamic> toJson() => _$AuthResponseModelToJson(this);
}

@JsonSerializable()
class RegisterRequestModel {
  final String firstName;
  final String lastName;
  final String email;
  final String password;
  final String? phone;
  final String? birthDate;
  final String? medicalHistory;

  RegisterRequestModel({
    required this.firstName,
    required this.lastName,
    required this.email,
    required this.password,
    this.phone,
    this.birthDate,
    this.medicalHistory,
  });

  factory RegisterRequestModel.fromJson(Map<String, dynamic> json) => _$RegisterRequestModelFromJson(json);
  Map<String, dynamic> toJson() => _$RegisterRequestModelToJson(this);
}

@JsonSerializable()
class RegisterDoctorRequestModel {
  final String firstName;
  final String lastName;
  final String email;
  final String password;
  final String specialtyName;
  final int experienceYears;
  final double consultationPrice;
  final int? slotDurationMinutes;
  final int? bufferTimeMinutes;
  final String? location;
  final String? bio;
  final String? photoUrl;

  RegisterDoctorRequestModel({
    required this.firstName,
    required this.lastName,
    required this.email,
    required this.password,
    required this.specialtyName,
    required this.experienceYears,
    required this.consultationPrice,
    this.slotDurationMinutes,
    this.bufferTimeMinutes,
    this.location,
    this.bio,
    this.photoUrl,
  });

  factory RegisterDoctorRequestModel.fromJson(Map<String, dynamic> json) => _$RegisterDoctorRequestModelFromJson(json);
  Map<String, dynamic> toJson() => _$RegisterDoctorRequestModelToJson(this);
}
