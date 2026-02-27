import 'dart:io';
import '../../data/models/auth_models.dart';

abstract class AuthRepository {
  Future<AuthResponseModel> login(String email, String password);
  Future<AuthResponseModel> register(RegisterRequestModel request, {File? photo});
  Future<AuthResponseModel> registerDoctor(RegisterDoctorRequestModel request, {File? photo});
}
