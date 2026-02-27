import 'package:easy_localization/easy_localization.dart';
import 'package:flutter/material.dart';
import '../../../../core/constants/app_colors.dart';

class PatientProfilePage extends StatelessWidget {
  const PatientProfilePage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.background,
      body: SingleChildScrollView(
        child: Column(
          children: [
            _buildProfileHeader(context),
            Padding(
              padding: const EdgeInsets.all(24.0),
              child: Column(
                children: [
                  _buildMedicalStats(context),
                  const SizedBox(height: 30),
                  _buildMenuSection(context, 'profile.personal_data'.tr(), [
                    _MenuTile(icon: Icons.person_outline, title: 'My Details', color: Colors.blue),
                    _MenuTile(icon: Icons.notifications_none, title: 'Notifications', color: Colors.orange),
                  ]),
                  const SizedBox(height: 20),
                  _buildMenuSection(context, 'profile.medical_history'.tr(), [
                    _MenuTile(icon: Icons.history, title: 'Appointment History', color: Colors.green),
                    _MenuTile(icon: Icons.description_outlined, title: 'Medical Records', color: Colors.purple),
                  ]),
                  const SizedBox(height: 30),
                  ElevatedButton.icon(
                    onPressed: () {
                      // TODO: Logout logic
                    },
                    icon: const Icon(Icons.logout),
                    label: Text('profile.logout'.tr()),
                    style: ElevatedButton.styleFrom(
                      backgroundColor: Colors.redAccent.withAlpha((0.1 * 255).toInt()),
                      foregroundColor: Colors.redAccent,
                      elevation: 0,
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildProfileHeader(BuildContext context) {
    return Container(
      padding: const EdgeInsets.only(top: 60, bottom: 30, left: 24, right: 24),
      decoration: const BoxDecoration(
        gradient: LinearGradient(
          colors: [AppColors.primary, Color(0xFF0056b3)],
          begin: Alignment.topCenter,
          end: Alignment.bottomCenter,
        ),
        borderRadius: BorderRadius.only(
          bottomLeft: Radius.circular(40),
          bottomRight: Radius.circular(40),
        ),
      ),
      child: Column(
        children: [
          const Center(
            child: Stack(
              children: [
                CircleAvatar(
                  radius: 50,
                  backgroundImage: NetworkImage('https://i.pravatar.cc/150?u=gloria'),
                ),
                Positioned(
                  bottom: 0,
                  right: 0,
                  child: CircleAvatar(
                    radius: 15,
                    backgroundColor: Colors.white,
                    child: Icon(Icons.camera_alt, size: 15, color: AppColors.primary),
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(height: 16),
          const Text(
            'Gloria Mckinney',
            style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 22),
          ),
          Text(
            'ID: PAX-2023-001',
            style: TextStyle(color: Colors.white.withAlpha((0.7 * 255).toInt())),
          ),
        ],
      ),
    );
  }

  Widget _buildMedicalStats(BuildContext context) {
    return Row(
      children: [
        Expanded(child: _StatCard(label: 'Weight', value: '62 kg', color: Colors.blue)),
        const SizedBox(width: 16),
        Expanded(child: _StatCard(label: 'Height', value: '168 cm', color: Colors.orange)),
        const SizedBox(width: 16),
        Expanded(child: _StatCard(label: 'Blood', value: 'O+', color: Colors.red)),
      ],
    );
  }

  Widget _buildMenuSection(BuildContext context, String title, List<Widget> items) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          title,
          style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16, color: AppColors.textPrimary),
        ),
        const SizedBox(height: 12),
        Container(
          decoration: BoxDecoration(
            color: Colors.white,
            borderRadius: BorderRadius.circular(20),
            boxShadow: [
              BoxShadow(
                color: Colors.black.withAlpha((0.03 * 255).toInt()),
                blurRadius: 10,
                offset: const Offset(0, 4),
              ),
            ],
          ),
          child: Column(children: items),
        ),
      ],
    );
  }
}

class _StatCard extends StatelessWidget {
  final String label;
  final String value;
  final Color color;

  const _StatCard({required this.label, required this.value, required this.color});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: color.withAlpha((0.1 * 255).toInt())),
      ),
      child: Column(
        children: [
          Text(label, style: const TextStyle(fontSize: 12, color: AppColors.textSecondary)),
          const SizedBox(height: 4),
          Text(value, style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16, color: color)),
        ],
      ),
    );
  }
}

class _MenuTile extends StatelessWidget {
  final IconData icon;
  final String title;
  final Color color;

  const _MenuTile({required this.icon, required this.title, required this.color});

  @override
  Widget build(BuildContext context) {
    return ListTile(
      leading: Container(
        padding: const EdgeInsets.all(8),
        decoration: BoxDecoration(
          color: color.withAlpha((0.1 * 255).toInt()),
          shape: BoxShape.circle,
        ),
        child: Icon(icon, color: color, size: 20),
      ),
      title: Text(title, style: const TextStyle(fontSize: 14, fontWeight: FontWeight.w500)),
      trailing: const Icon(Icons.chevron_right, size: 20, color: AppColors.secondary),
      onTap: () {},
    );
  }
}
