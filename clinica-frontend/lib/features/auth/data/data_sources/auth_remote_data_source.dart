import 'dart:io';
import 'package:dio/dio.dart';
import '../models/auth_models.dart';

class AuthRemoteDataSource {
  final Dio _dio;
  
  // Use 10.0.2.2 for Android emulator to access localhost
  static const String _baseUrl = "http://10.0.2.2:8080/api/v1/auth";

  AuthRemoteDataSource(this._dio);

  Future<AuthResponseModel> login(AuthRequestModel request) async {
    try {
      final response = await _dio.post(
        "$_baseUrl/login",
        data: request.toJson(),
      );
      return AuthResponseModel.fromJson(response.data);
    } on DioException catch (e) {
      throw _handleError(e);
    }
  }

  Future<AuthResponseModel> register(RegisterRequestModel request, {File? photo}) async {
    try {
      final Map<String, dynamic> data = request.toJson();
      
      if (photo != null) {
        final formData = FormData.fromMap({
          ...data,
          'photo': await MultipartFile.fromFile(photo.path, filename: 'profile.jpg'),
        });
        final response = await _dio.post("$_baseUrl/register", data: formData);
        return AuthResponseModel.fromJson(response.data);
      }

      final response = await _dio.post("$_baseUrl/register", data: data);
      return AuthResponseModel.fromJson(response.data);
    } on DioException catch (e) {
      throw _handleError(e);
    }
  }

  Future<AuthResponseModel> registerDoctor(RegisterDoctorRequestModel request, {File? photo}) async {
    try {
      final Map<String, dynamic> data = request.toJson();
      
      if (photo != null) {
        final formData = FormData.fromMap({
          ...data,
          'photo': await MultipartFile.fromFile(photo.path, filename: 'doctor_profile.jpg'),
        });
        final response = await _dio.post("$_baseUrl/register/doctor", data: formData);
        return AuthResponseModel.fromJson(response.data);
      }

      final response = await _dio.post("$_baseUrl/register/doctor", data: data);
      return AuthResponseModel.fromJson(response.data);
    } on DioException catch (e) {
      throw _handleError(e);
    }
  }

  Exception _handleError(DioException e) {
    if (e.response != null) {
      return Exception(e.response?.data['message'] ?? 'Error del servidor');
    }
    return Exception('Error de conexión: ${e.message}');
  }
}
