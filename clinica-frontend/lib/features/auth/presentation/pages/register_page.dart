import 'dart:io';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';
import 'package:easy_localization/easy_localization.dart';
import 'package:latlong2/latlong.dart';
import '../../../../core/constants/app_colors.dart';
import '../bloc/auth_bloc.dart';
import '../../data/models/auth_models.dart';
import '../../../../shared/widgets/custom_text_field.dart';
import '../../../../shared/widgets/medical_role_selector.dart';
import '../../../../shared/widgets/profile_photo_selector.dart';
import '../../../../shared/widgets/map_location_selector.dart';

class RegisterPage extends StatefulWidget {
  const RegisterPage({super.key});

  @override
  State<RegisterPage> createState() => _RegisterPageState();
}

class _RegisterPageState extends State<RegisterPage> {
  final _formKey = GlobalKey<FormState>();
  
  // Controllers
  final _nameController = TextEditingController();
  final _emailController = TextEditingController();
  final _passwordController = TextEditingController();
  final _phoneController = TextEditingController();
  
  // Patient fields
  final _birthDateController = TextEditingController();
  final _medicalHistoryController = TextEditingController();
  
  // Doctor fields
  final _specialtyController = TextEditingController();
  final _experienceYearsController = TextEditingController(text: '0');
  final _consultationPriceController = TextEditingController(text: '0.0');
  final _slotDurationController = TextEditingController(text: '30');
  final _bufferTimeController = TextEditingController(text: '5');
  final _bioController = TextEditingController();

  // New State
  String _selectedRole = 'patient';
  File? _profileImage;
  LatLng? _officeLocation;
  String? _officeAddress;

  @override
  void dispose() {
    _nameController.dispose();
    _emailController.dispose();
    _passwordController.dispose();
    _phoneController.dispose();
    _birthDateController.dispose();
    _medicalHistoryController.dispose();
    _specialtyController.dispose();
    _experienceYearsController.dispose();
    _consultationPriceController.dispose();
    _slotDurationController.dispose();
    _bufferTimeController.dispose();
    _bioController.dispose();
    super.dispose();
  }

  void _handleRegister() {
    if (!_formKey.currentState!.validate()) return;

    final nameParts = _nameController.text.trim().split(' ');
    final firstName = nameParts.first;
    final lastName = nameParts.length > 1 ? nameParts.sublist(1).join(' ') : '';

    if (_selectedRole == 'doctor') {
      final request = RegisterDoctorRequestModel(
        firstName: firstName,
        lastName: lastName,
        email: _emailController.text.trim(),
        password: _passwordController.text.trim(),
        specialtyName: _specialtyController.text.trim(),
        experienceYears: int.tryParse(_experienceYearsController.text) ?? 0,
        consultationPrice: double.tryParse(_consultationPriceController.text) ?? 0.0,
        slotDurationMinutes: int.tryParse(_slotDurationController.text) ?? 30,
        bufferTimeMinutes: int.tryParse(_bufferTimeController.text) ?? 5,
        location: _officeAddress ?? '',
        bio: _bioController.text.trim(),
      );
      context.read<AuthBloc>().add(RegisterDoctorRequested(request, photo: _profileImage));
    } else {
      final request = RegisterRequestModel(
        firstName: firstName,
        lastName: lastName,
        email: _emailController.text.trim(),
        password: _passwordController.text.trim(),
        phone: _phoneController.text.trim(),
        birthDate: _birthDateController.text.trim(),
        medicalHistory: _medicalHistoryController.text.trim(),
      );
      context.read<AuthBloc>().add(RegisterRequested(request, photo: _profileImage));
    }
  }

  @override
  Widget build(BuildContext context) {
    final isDoctor = _selectedRole == 'doctor';

    return Scaffold(
      backgroundColor: AppColors.background,
      appBar: AppBar(
        backgroundColor: Colors.transparent,
        foregroundColor: AppColors.textPrimary,
        elevation: 0,
        leading: IconButton(
          icon: const Icon(Icons.arrow_back_ios_new, size: 20),
          onPressed: () => context.pop(),
        ),
      ),
      body: SafeArea(
        child: SingleChildScrollView(
          physics: const BouncingScrollPhysics(),
          padding: const EdgeInsets.symmetric(horizontal: 24.0, vertical: 16),
          child: Form(
            key: _formKey,
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  'Crear Cuenta'.tr(),
                  style: Theme.of(context).textTheme.headlineMedium?.copyWith(
                    fontWeight: FontWeight.w900,
                    color: AppColors.primary,
                    letterSpacing: -0.5,
                  ),
                ),
                const SizedBox(height: 8),
                Text(
                  'Únete a Vitalis para una mejor gestión de salud'.tr(),
                  style: Theme.of(context).textTheme.bodyLarge?.copyWith(
                    color: AppColors.textSecondary,
                  ),
                ),
                const SizedBox(height: 32),
                
                // Photo Selector
                ProfilePhotoSelector(
                  image: _profileImage,
                  onImageSelected: (file) => setState(() => _profileImage = file),
                ),
                const SizedBox(height: 32),
                
                // Role Selector
                MedicalRoleSelector(
                  selectedRole: _selectedRole,
                  onRoleChanged: (role) => setState(() => _selectedRole = role),
                ),
                const SizedBox(height: 32),
                
                // Commons Fields
                Text('Información Personal'.tr(), style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16, color: AppColors.primary)),
                const SizedBox(height: 16),
                CustomTextField(
                  controller: _nameController,
                  label: 'Nombre Completo'.tr(),
                  prefixIcon: Icons.person_outline,
                  validator: (v) => v!.isEmpty ? 'Campo requerido' : null,
                ),
                const SizedBox(height: 20),
                CustomTextField(
                  controller: _phoneController,
                  label: 'Teléfono'.tr(),
                  prefixIcon: Icons.phone_outlined,
                  keyboardType: TextInputType.phone,
                  validator: (v) => v!.isEmpty ? 'Campo requerido' : null,
                ),
                
                // Conditional Fields
                AnimatedCrossFade(
                  duration: const Duration(milliseconds: 400),
                  crossFadeState: isDoctor ? CrossFadeState.showSecond : CrossFadeState.showFirst,
                  firstChild: _buildPatientFields(),
                  secondChild: _buildDoctorFields(),
                ),
                
                const SizedBox(height: 32),
                Text('Seguridad'.tr(), style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16, color: AppColors.primary)),
                const SizedBox(height: 16),
                CustomTextField(
                  controller: _emailController,
                  label: 'Correo Electrónico'.tr(),
                  prefixIcon: Icons.email_outlined,
                  keyboardType: TextInputType.emailAddress,
                  validator: (v) => !v!.contains('@') ? 'Email inválido' : null,
                ),
                const SizedBox(height: 20),
                CustomTextField(
                  controller: _passwordController,
                  label: 'Contraseña'.tr(),
                  prefixIcon: Icons.lock_outline,
                  isPassword: true,
                  validator: (v) => v!.length < 6 ? 'Mínimo 6 caracteres' : null,
                ),
                
                const SizedBox(height: 40),
                
                BlocConsumer<AuthBloc, AuthState>(
                  listener: (context, state) {
                    if (state is AuthAuthenticated) {
                      context.go(state.user.role == 'doctor' ? '/doctor-home' : '/patient-home');
                    } else if (state is AuthError) {
                      ScaffoldMessenger.of(context).showSnackBar(
                        SnackBar(
                          content: Text(state.message), 
                          backgroundColor: AppColors.danger,
                          behavior: SnackBarBehavior.floating,
                          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
                        ),
                      );
                    }
                  },
                  builder: (context, state) {
                    return ElevatedButton(
                      onPressed: state is AuthLoading ? null : _handleRegister,
                      child: state is AuthLoading
                          ? const SizedBox(
                              height: 20,
                              width: 20,
                              child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white),
                            )
                          : Text('Registrarse'.tr()),
                    );
                  },
                ),
                const SizedBox(height: 40),
              ],
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildPatientFields() {
    return Column(
      children: [
        const SizedBox(height: 20),
        CustomTextField(
          controller: _birthDateController,
          label: 'Fecha de Nacimiento'.tr(),
          prefixIcon: Icons.calendar_today_outlined,
          readOnly: true,
          onTap: () async {
            DateTime? pickedDate = await showDatePicker(
              context: context,
              initialDate: DateTime.now().subtract(const Duration(days: 365 * 20)),
              firstDate: DateTime(1900),
              lastDate: DateTime.now(),
              builder: (context, child) {
                return Theme(
                  data: Theme.of(context).copyWith(
                    colorScheme: const ColorScheme.light(primary: AppColors.primary),
                  ),
                  child: child!,
                );
              },
            );
            if (pickedDate != null) {
              _birthDateController.text = pickedDate.toString().split(' ')[0];
            }
          },
        ),
        const SizedBox(height: 20),
        CustomTextField(
          controller: _medicalHistoryController,
          label: 'Historial Médico (Opcional)'.tr(),
          prefixIcon: Icons.history_outlined,
          maxLines: 3,
        ),
      ],
    );
  }

  Widget _buildDoctorFields() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const SizedBox(height: 32),
        Text('Información Profesional'.tr(), style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16, color: AppColors.primary)),
        const SizedBox(height: 16),
        CustomTextField(
          controller: _specialtyController,
          label: 'Especialidad'.tr(),
          prefixIcon: Icons.category_outlined,
          validator: (v) => _selectedRole == 'doctor' && v!.isEmpty ? 'Campo requerido' : null,
        ),
        const SizedBox(height: 20),
        Row(
          children: [
            Expanded(
              child: CustomTextField(
                controller: _experienceYearsController,
                label: 'Años Exp.'.tr(),
                keyboardType: TextInputType.number,
              ),
            ),
            const SizedBox(width: 16),
            Expanded(
              child: CustomTextField(
                controller: _consultationPriceController,
                label: 'Precio (\$)'.tr(),
                keyboardType: TextInputType.number,
              ),
            ),
          ],
        ),
        const SizedBox(height: 20),
        MapLocationSelector(
          initialLocation: _officeLocation,
          initialAddress: _officeAddress,
          onLocationSelected: (point, address) {
            setState(() {
              _officeLocation = point;
              _officeAddress = address;
            });
          },
        ),
        const SizedBox(height: 20),
        CustomTextField(
          controller: _bioController,
          label: 'Breve Biografía'.tr(),
          prefixIcon: Icons.info_outline,
          maxLines: 3,
        ),
      ],
    );
  }
}
