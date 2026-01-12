import { Ionicons } from '@expo/vector-icons';
import { useState } from 'react';
import {
  ActivityIndicator,
  Alert,
  KeyboardAvoidingView,
  Platform,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  TouchableOpacity,
  View
} from 'react-native';
import { BORDER_RADIUS, FONT_SIZE, SPACING } from '../../constants/theme';
import { useAuth } from '../../context/AuthContext';
import { useTheme } from '../../context/ThemeContext';

/**
 * Actualizar contraseña
 */
export default function CambiarContraseniaScreen({ navigation }: any) {
  const { user } = useAuth();
  const { theme } = useTheme();
  const colors = theme.colors;

  // URL Base de la API
  const API_BASE = 'https://packetworld.izekki.me//';

  // Estados del formulario
  const [loading, setLoading] = useState(false);
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');

  // Estados para controlar la visibilidad de los caracteres
  const [showCurrent, setShowCurrent] = useState(false);
  const [showNew, setShowNew] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);


  const handleUpdate = async () => {
    // Validaciones 
    if (!currentPassword.trim() || !newPassword.trim() || !confirmPassword.trim()) {
      Alert.alert('Campos Incompletos', 'Por favor, llena todos los campos para continuar.');
      return;
    }
    if (newPassword !== confirmPassword) {
      Alert.alert('Error de Validación', 'La nueva contraseña y su confirmación no coinciden.');
      return;
    }
    if (newPassword.length < 4) {
      Alert.alert('Seguridad', 'La nueva contraseña debe tener al menos 4 caracteres.');
      return;
    }

    setLoading(true);

    try {
      const endpoint = `${API_BASE}api/colaboradores/cambiarContrasenia`;

      const details: Record<string, string> = {
        'numeroPersonal': user?.numeroPersonal || '',
        'contraseniaActual': currentPassword,
        'contraseniaNueva': newPassword,
      };

      const formBody = Object.keys(details)
        .map(key => encodeURIComponent(key) + '=' + encodeURIComponent(details[key]))
        .join('&');

      //console.log('Enviando petición PUT a:', endpoint);

      const response = await fetch(endpoint, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8'
        },
        body: formBody,
      });

      const textResponse = await response.text();
      //console.log('Respuesta cruda del servidor:', textResponse);

      let data;
      try {
        data = JSON.parse(textResponse);
      } catch (jsonError) {
        throw new Error('El servidor devolvió una respuesta no válida (no JSON).');
      }

      if (!data.error) {
        Alert.alert('Éxito', data.mensaje || 'Contraseña actualizada correctamente.', [
          { text: 'Entendido', onPress: () => navigation.goBack() }
        ]);
      } else {
        Alert.alert('No se pudo actualizar', data.mensaje || 'La contraseña actual podría ser incorrecta.');
      }

    } catch (error: any) {
      //console.error('Error en cambio de contraseña:', error);
      Alert.alert(
        'Fallo de Conexión', 
        'No pudimos contactar con el servidor. Verifica tu conexión a internet.'
      );
    } finally {
      setLoading(false);
    }
  };

  /**
   * Renderiza los campos de texto con el icono de ojo para alternar visibilidad
   */
  const renderPasswordField = (
    label: string, 
    value: string, 
    setValue: (t: string) => void, 
    show: boolean, 
    setShow: (b: boolean) => void
  ) => (
    <View style={styles.inputGroup}>
      <Text style={[styles.label, { color: colors.textSecondary }]}>{label}</Text>
      <View style={[styles.inputWrapper, { borderColor: colors.border, backgroundColor: colors.card }]}>
        <TextInput
          style={[styles.input, { color: colors.text }]}
          secureTextEntry={!show}
          value={value}
          onChangeText={setValue}
          placeholder="Escribe aquí..."
          placeholderTextColor={colors.textSecondary}
          autoCapitalize="none"
        />
        <TouchableOpacity onPress={() => setShow(!show)} style={styles.eyeBtn}>
          <Ionicons name={show ? "eye-off" : "eye"} size={22} color={colors.textSecondary} />
        </TouchableOpacity>
      </View>
    </View>
  );

  return (
    <KeyboardAvoidingView 
      style={{ flex: 1 }} 
      behavior={Platform.OS === "ios" ? "padding" : undefined}
    >
      <ScrollView contentContainerStyle={[styles.container, { backgroundColor: colors.background }]}>
        
        <View style={styles.headerArea}>
          <View style={[styles.iconCircle, { backgroundColor: colors.primary + '15' }]}>
            <Ionicons name="lock-open-outline" size={45} color={colors.primary} />
          </View>
          <Text style={[styles.headerTitle, { color: colors.text }]}>Actualización de Seguridad</Text>
          <Text style={[styles.headerSubtitle, { color: colors.textSecondary }]}>
            Por favor, ingresa tus credenciales para realizar el cambio de contraseña en el sistema.
          </Text>
        </View>

        <View style={styles.formContainer}>
          {renderPasswordField('Contraseña Actual', currentPassword, setCurrentPassword, showCurrent, setShowCurrent)}
          
          <View style={[styles.separator, { backgroundColor: colors.border }]} />

          {renderPasswordField('Contraseña Nueva', newPassword, setNewPassword, showNew, setShowNew)}
          {renderPasswordField('Confirmar Contraseña Nueva', confirmPassword, setConfirmPassword, showConfirm, setShowConfirm)}

          <TouchableOpacity 
            style={[styles.submitBtn, { backgroundColor: colors.primary }]}
            onPress={handleUpdate}
            disabled={loading}
            activeOpacity={0.8}
          >
            {loading ? (
              <ActivityIndicator color="#FFF" />
            ) : (
              <View style={styles.btnInner}>
                <Ionicons name="shield-checkmark" size={20} color="#FFF" />
                <Text style={styles.submitBtnText}>Confirmar Cambio</Text>
              </View>
            )}
          </TouchableOpacity>
        </View>

        <View style={{ height: 40 }} />
      </ScrollView>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  container: {
    flexGrow: 1,
    padding: SPACING.l,
  },
  headerArea: {
    alignItems: 'center',
    marginBottom: SPACING.xl,
    marginTop: SPACING.m,
  },
  iconCircle: {
    width: 90,
    height: 90,
    borderRadius: 45,
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: SPACING.m,
  },
  headerTitle: {
    fontSize: FONT_SIZE.xl,
    fontWeight: 'bold',
    marginBottom: SPACING.s,
    textAlign: 'center',
  },
  headerSubtitle: {
    fontSize: FONT_SIZE.s,
    textAlign: 'center',
    lineHeight: 22,
    paddingHorizontal: SPACING.m,
  },
  formContainer: {
    width: '100%',
  },
  inputGroup: {
    marginBottom: SPACING.l,
  },
  label: {
    fontSize: FONT_SIZE.xs,
    fontWeight: 'bold',
    marginBottom: 8,
    textTransform: 'uppercase',
  },
  inputWrapper: {
    flexDirection: 'row',
    alignItems: 'center',
    borderWidth: 1,
    borderRadius: BORDER_RADIUS.m,
    paddingHorizontal: SPACING.m,
    height: 55,
  },
  input: {
    flex: 1,
    height: '100%',
    fontSize: FONT_SIZE.m,
  },
  eyeBtn: {
    padding: SPACING.s,
  },
  separator: {
    height: 1,
    width: '100%',
    marginBottom: SPACING.l,
    opacity: 0.2,
  },
  submitBtn: {
    height: 56,
    borderRadius: BORDER_RADIUS.m,
    justifyContent: 'center',
    alignItems: 'center',
    marginTop: SPACING.m,
    elevation: 3,
    shadowColor: "#000",
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
  },
  btnInner: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 10,
  },
  submitBtnText: {
    color: '#FFF',
    fontSize: FONT_SIZE.l,
    fontWeight: 'bold',
  },
});