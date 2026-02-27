import 'package:easy_localization/easy_localization.dart';
import 'package:flutter/material.dart';
import 'core/theme/app_theme.dart';
import 'core/utils/app_router.dart';

import 'package:dio/dio.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'features/auth/data/data_sources/auth_remote_data_source.dart';
import 'features/auth/data/repositories/auth_repository_impl.dart';
import 'features/auth/presentation/bloc/auth_bloc.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await EasyLocalization.ensureInitialized();

  final dio = Dio();
  final authRemoteDataSource = AuthRemoteDataSource(dio);
  final authRepository = AuthRepositoryImpl(authRemoteDataSource);

  runApp(
    EasyLocalization(
      supportedLocales: const [Locale('es'), Locale('en')],
      path: 'assets/translations',
      fallbackLocale: const Locale('es'),
      child: MultiBlocProvider(
        providers: [
          BlocProvider<AuthBloc>(
            create: (context) => AuthBloc(authRepository),
          ),
        ],
        child: const VitalisApp(),
      ),
    ),
  );
}

class VitalisApp extends StatelessWidget {
  const VitalisApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp.router(
      title: 'Vitalis',
      debugShowCheckedModeBanner: false,
      localizationsDelegates: context.localizationDelegates,
      supportedLocales: context.supportedLocales,
      locale: context.locale,
      theme: AppTheme.lightTheme,
      routerConfig: AppRouter.router,
    );
  }
}
