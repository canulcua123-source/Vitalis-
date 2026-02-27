import 'dart:io';
import '../data_sources/auth_remote_data_source.dart';
import '../models/auth_models.dart';
import '../../domain/repositories/auth_repository.dart';

class AuthRepositoryImpl implements AuthRepository {
  final AuthRemoteDataSource _remoteDataSource;

  AuthRepositoryImpl(this._remoteDataSource);

  @override
  Future<AuthResponseModel> login(String email, String password) async {
    final request = AuthRequestModel(email: email, password: password);
    return await _remoteDataSource.login(request);
  }

  @override
  Future<AuthResponseModel> register(RegisterRequestModel request, {File? photo}) async {
    return await _remoteDataSource.register(request, photo: photo);
  }

  @override
  Future<AuthResponseModel> registerDoctor(RegisterDoctorRequestModel request, {File? photo}) async {
    return await _remoteDataSource.registerDoctor(request, photo: photo);
  }
}
