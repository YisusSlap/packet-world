import { Ionicons } from '@expo/vector-icons';
import { Button, StyleSheet, Switch, Text, TouchableOpacity, View } from 'react-native';
import { BORDER_RADIUS, FONT_SIZE, SPACING } from '../constants/theme';
import { useAuth } from '../context/AuthContext';
import { useTheme } from '../context/ThemeContext';

export default function SettingsScreen({ navigation }: any) {
  const { logout } = useAuth();
  const { theme, toggleTheme, isDark } = useTheme();
  const colors = theme.colors;

  return (
    <View style={[styles.container, { backgroundColor: colors.background }]}>
      <Text style={[styles.title, { color: colors.text }]}>Ajustes</Text>

      {/* Sección de Apariencia */}
      <View style={[styles.section, { backgroundColor: colors.card, borderColor: colors.border }]}>
        <Text style={[styles.sectionHeader, { color: colors.textSecondary }]}>
          APARIENCIA
        </Text>
        
        <View style={styles.row}>
          <Text style={[styles.label, { color: colors.text }]}>Modo Oscuro</Text>
          <Switch
            value={isDark}
            onValueChange={toggleTheme}
            trackColor={{ false: '#767577', true: colors.primary }}
            thumbColor={isDark ? "#fff" : "#f4f3f4"}
          />
        </View>
      </View>

      {/* Sección de Seguridad */}
      <View style={[styles.section, { backgroundColor: colors.card, borderColor: colors.border }]}>
        <Text style={[styles.sectionHeader, { color: colors.textSecondary }]}>
          SEGURIDAD
        </Text>
        
        <TouchableOpacity 
          style={styles.row}
          onPress={() => navigation.navigate('ChangePassword')}
          activeOpacity={0.7}
        >
          <View style={styles.labelWithIcon}>
            <Ionicons name="lock-closed-outline" size={20} color={colors.text} style={{ marginRight: 10 }} />
            <Text style={[styles.label, { color: colors.text }]}>Actualizar Contraseña</Text>
          </View>
          <Ionicons name="chevron-forward" size={20} color={colors.textSecondary} />
        </TouchableOpacity>
      </View>

      {/* Sección de Cuenta */}
      <View style={[styles.section, { backgroundColor: colors.card, borderColor: colors.border }]}>
        <Text style={[styles.sectionHeader, { color: colors.textSecondary }]}>
          CUENTA
        </Text>
        <View style={styles.buttonContainer}>
          <Button 
            title="Cerrar Sesión" 
            onPress={logout} 
            color={colors.error} 
          />
        </View>
      </View>

      <Text style={[styles.versionText, { color: colors.textSecondary }]}>
        Packet-World Móvil v1.0.0
      </Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: SPACING.m,
  },
  title: {
    fontSize: FONT_SIZE.xxl,
    fontWeight: 'bold',
    marginBottom: SPACING.l,
    marginTop: SPACING.m,
  },
  section: {
    marginBottom: SPACING.l,
    borderRadius: BORDER_RADIUS.m,
    borderWidth: 1,
    padding: SPACING.m,
    elevation: 2,
    shadowColor: "#000",
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.1,
    shadowRadius: 2,
  },
  sectionHeader: {
    fontSize: FONT_SIZE.xs,
    fontWeight: 'bold',
    marginBottom: SPACING.s,
    letterSpacing: 1,
  },
  row: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: SPACING.s,
  },
  labelWithIcon: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  label: {
    fontSize: FONT_SIZE.m,
  },
  buttonContainer: {
    marginTop: SPACING.xs,
  },
  versionText: {
    textAlign: 'center',
    fontSize: FONT_SIZE.s,
    marginTop: 'auto',
    marginBottom: SPACING.m,
  },
});