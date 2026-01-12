import { useState } from 'react';
import {
  ActivityIndicator,
  Alert,
  Button,
  Image,
  KeyboardAvoidingView,
  Platform,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  View
} from 'react-native';
import { BORDER_RADIUS, FONT_SIZE, SPACING } from '../constants/theme';
import { useAuth } from '../context/AuthContext';
import { useTheme } from '../context/ThemeContext';
import type { LoginScreenProps } from '../navigation/types';

/**
 * Pantalla de Inicio de Sesión (Login)
 * Presenta el acceso para los conductores con la identidad visual de Packet World.
 * Incluye el logotipo corporativo y validaciones básicas de entrada.
 */
export default function LoginScreen({ navigation }: LoginScreenProps) {
  const { login, isLoading } = useAuth(); 
  const { theme } = useTheme();
  const colors = theme.colors;

  const [personalId, setPersonalId] = useState('');
  const [password, setPassword] = useState('');

  /**
   * Maneja el intento de inicio de sesión validando campos requeridos
   */
  const handleLogin = () => {
    if (!personalId.trim() || !password.trim()) {
      Alert.alert(
        'Campos Incompletos', 
        'Por favor ingresa tu número de personal y contraseña para acceder al sistema.'
      );
      return;
    }
    login(personalId.trim(), password);
  };

  return (
    <KeyboardAvoidingView 
      behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
      style={{ flex: 1 }}
    >
      <ScrollView 
        contentContainerStyle={[styles.container, { backgroundColor: colors.background }]}
        keyboardShouldPersistTaps="handled"
      >
        {/* --- CABECERA: LOGOTIPO Y TÍTULO --- */}
        <View style={styles.headerContainer}>
          <Image 
            source={require('../../assets/icono_logo.png')} 
            style={styles.logo}
            resizeMode="contain"
          />
          <Text style={[styles.title, { color: colors.text }]}>Packet World</Text>
          <Text style={[styles.subtitle, { color: colors.textSecondary }]}>
            Gestión Logística de Envíos
          </Text>
        </View>

        {/* --- FORMULARIO DE ACCESO --- */}
        <View style={styles.form}>
          <View style={styles.inputGroup}>
            <Text style={[styles.label, { color: colors.text }]}>Número de Personal</Text>
            <TextInput 
              style={[styles.input, { 
                borderColor: colors.border, 
                backgroundColor: colors.card,
                color: colors.text
              }]}
              placeholder="Ej. COND001"
              placeholderTextColor={colors.textSecondary}
              value={personalId}
              onChangeText={setPersonalId}
              autoCapitalize="characters"
              autoCorrect={false}
            />
          </View>

          <View style={styles.inputGroup}>
            <Text style={[styles.label, { color: colors.text }]}>Contraseña</Text>
            <TextInput 
              style={[styles.input, { 
                borderColor: colors.border, 
                backgroundColor: colors.card,
                color: colors.text
              }]}
              placeholder="••••••••"
              placeholderTextColor={colors.textSecondary}
              value={password}
              onChangeText={setPassword}
              secureTextEntry
            />
          </View>

          <View style={styles.buttonContainer}>
            {isLoading ? (
              <ActivityIndicator size="large" color={colors.primary} />
            ) : (
              <View style={[styles.btnWrapper, { backgroundColor: colors.primary }]}>
                <Button 
                  title="Iniciar Sesión" 
                  onPress={handleLogin} 
                  color={Platform.OS === 'ios' ? '#FFF' : colors.primary} 
                />
              </View>
            )}
          </View>
        </View>
        
        {/* --- PIE DE PÁGINA --- */}
        <View style={styles.footer}>
          <Text style={[styles.footerText, { color: colors.textSecondary }]}>
            Conectado a Servidor Central
          </Text>
          <Text style={[styles.footerSubText, { color: colors.textSecondary }]}>
            © 2026 Packet World - Área de Transporte
          </Text>
        </View>
      </ScrollView>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  container: {
    flexGrow: 1,
    justifyContent: 'center',
    padding: SPACING.xl,
  },
  headerContainer: {
    alignItems: 'center',
    marginBottom: SPACING.xl,
  },
  logo: {
    width: 130,
    height: 130,
    marginBottom: SPACING.m,
  },
  title: {
    fontSize: FONT_SIZE.xxl,
    fontWeight: 'bold',
    textAlign: 'center',
    marginBottom: SPACING.xs,
    letterSpacing: 0.5,
  },
  subtitle: {
    fontSize: FONT_SIZE.s,
    textAlign: 'center',
    marginBottom: SPACING.l,
    opacity: 0.7,
  },
  form: {
    width: '100%',
  },
  inputGroup: {
    marginBottom: SPACING.m,
  },
  label: {
    fontSize: FONT_SIZE.xs,
    marginBottom: SPACING.s,
    fontWeight: 'bold',
    textTransform: 'uppercase',
  },
  input: {
    borderWidth: 1,
    paddingHorizontal: SPACING.m,
    borderRadius: BORDER_RADIUS.m,
    fontSize: FONT_SIZE.m,
    height: 56,
  },
  buttonContainer: {
    marginTop: SPACING.l,
  },
  btnWrapper: {
    borderRadius: BORDER_RADIUS.m,
    overflow: 'hidden',
    height: 54,
    justifyContent: 'center',
    elevation: 3, // Sombra para Android
    shadowColor: '#000', // Sombra para iOS
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
  },
  footer: {
    marginTop: SPACING.xxl,
    alignItems: 'center',
  },
  footerText: {
    fontSize: FONT_SIZE.xs,
    fontWeight: '600',
  },
  footerSubText: {
    fontSize: 10,
    marginTop: 4,
    opacity: 0.6,
  }
});