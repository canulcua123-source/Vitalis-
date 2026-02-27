import 'package:go_router/go_router.dart';
import '../../features/auth/presentation/pages/login_page.dart';
import '../../features/auth/presentation/pages/register_page.dart';
import '../../features/auth/presentation/pages/splash_screen.dart';
import '../../features/patient/presentation/pages/home_page.dart';
import '../../features/patient/presentation/pages/profile_page.dart';

class AppRouter {
  static final router = GoRouter(
    initialLocation: '/splash',
    routes: [
      GoRoute(
        path: '/splash',
        builder: (context, state) => const SplashScreen(),
      ),
      GoRoute(
        path: '/login',
        builder: (context, state) => const LoginPage(),
      ),
      GoRoute(
        path: '/register',
        builder: (context, state) => const RegisterPage(),
      ),
      GoRoute(
        path: '/patient-home',
        builder: (context, state) => const PatientHomePage(),
      ),
      GoRoute(
        path: '/patient-profile',
        builder: (context, state) => const PatientProfilePage(),
      ),
    ],
  );
}
