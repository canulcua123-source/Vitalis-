import 'package:flutter_test/flutter_test.dart';
import 'package:vitalis_app/main.dart';

void main() {
  testWidgets('Smoke test - Verify App Starts', (WidgetTester tester) async {
    // Build our app and trigger a frame.
    // Note: Since we use EasyLocalization, we might need more setup for complex widget tests,
    // but for a basic smoke test, checking the app type is enough.
    await tester.pumpWidget(const VitalisApp());

    expect(find.byType(VitalisApp), findsOneWidget);
  });
}
