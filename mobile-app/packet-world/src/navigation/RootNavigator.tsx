import { Ionicons } from '@expo/vector-icons';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import React from 'react';

import { useAuth } from '../context/AuthContext';
import { useTheme } from '../context/ThemeContext';
import type { AuthStackParamList, HomeStackParamList, RootTabParamList } from './types';

// Pantallas
import HomeScreen from '../screens/HomeScreen';
import LoginScreen from '../screens/LoginScreen';
import SettingsScreen from '../screens/SettingsScreen';
import ShipmentDetailScreen from '../screens/envios/EnvioDetallesScreen';
import ShipmentsListScreen from '../screens/envios/EnviosListaScreen';
import ChangePasswordScreen from '../screens/profile/CambiarContraseniaScreen';
import EditProfileScreen from '../screens/profile/EditarPerfilScreen';

// ----------------------------------------------------------------------
// Stack de Autenticación
// ----------------------------------------------------------------------
const AuthStack = createNativeStackNavigator<AuthStackParamList>();

function AuthNavigator() {
  const { theme } = useTheme();
  const colors = theme.colors;

  return (
    <AuthStack.Navigator 
      screenOptions={{
        headerStyle: { backgroundColor: colors.background },
        headerTintColor: colors.text,
        contentStyle: { backgroundColor: colors.background },
        headerShown: false 
      }}
    >
      <AuthStack.Screen name="Login" component={LoginScreen} />
    </AuthStack.Navigator>
  );
}

// ----------------------------------------------------------------------
// Stack de Inicio
// ----------------------------------------------------------------------
const HomeStack = createNativeStackNavigator<HomeStackParamList>();

function HomeStackNavigator() {
  const { theme } = useTheme();
  const colors = theme.colors;

  return (
    <HomeStack.Navigator
      screenOptions={{
        headerStyle: { backgroundColor: colors.card },
        headerTintColor: colors.text,
        headerTitleStyle: { fontWeight: 'bold' },
        contentStyle: { backgroundColor: colors.background },
        headerBackTitleVisible: false,
      }}
    >
      <HomeStack.Screen 
        name="Home" 
        component={HomeScreen} 
        options={{ headerShown: false }} 
      />
      <HomeStack.Screen 
        name="ShipmentsList" 
        component={ShipmentsListScreen} 
        options={{ title: 'Mis Envíos' }}
      />
      <HomeStack.Screen 
        name="ShipmentDetail" 
        component={ShipmentDetailScreen} 
        options={{ title: 'Detalle del Envío' }}
      />
      <HomeStack.Screen 
        name="EditProfile" 
        component={EditProfileScreen} 
        options={{ title: 'Editar Perfil' }}
      />
    </HomeStack.Navigator>
  );
}


// ----------------------------------------------------------------------
// Stack de Ajustes
// ----------------------------------------------------------------------
function SettingsStackNavigator() {
  const { theme } = useTheme();
  const colors = theme.colors;

  return (
    <HomeStack.Navigator
      screenOptions={{
        headerStyle: { backgroundColor: colors.card },
        headerTintColor: colors.text,
        contentStyle: { backgroundColor: colors.background },
        headerBackTitleVisible: false,
      }}
    >
      <HomeStack.Screen 
        name="Home" 
        component={SettingsScreen} 
        options={{ title: 'Configuración' }} 
      />
      <HomeStack.Screen 
        name="ChangePassword" 
        component={ChangePasswordScreen} 
        options={{ title: 'Actualizar Contraseña' }} 
      />
    </HomeStack.Navigator>
  );
}

// ----------------------------------------------------------------------
// Tabs Principales
// ----------------------------------------------------------------------
const Tab = createBottomTabNavigator<RootTabParamList>();

function MainTabNavigator() {
  const { theme } = useTheme(); 
  const colors = theme.colors;

  return (
    <Tab.Navigator
      screenOptions={({ route }) => ({
        headerStyle: {
          backgroundColor: colors.card,
          borderBottomWidth: 1,
          borderBottomColor: colors.border,
          elevation: 0,
          shadowOpacity: 0,
        },
        headerTintColor: colors.text,
        headerTitleStyle: { fontWeight: 'bold' },
        tabBarStyle: {
          backgroundColor: colors.card,
          borderTopColor: colors.border,
        },
        tabBarActiveTintColor: colors.primary,
        tabBarInactiveTintColor: colors.textSecondary,
        
        tabBarIcon: ({ focused, color, size }) => {
          let iconName: React.ComponentProps<typeof Ionicons>['name'];
          if (route.name === 'InicioStack') {
            iconName = focused ? 'home' : 'home-outline';
          } else if (route.name === 'Ajustes') {
            iconName = focused ? 'settings' : 'settings-outline';
          } else {
            iconName = 'alert-circle';
          }
          return <Ionicons name={iconName} size={size} color={color} />;
        },
      })}
    >
      <Tab.Screen 
        name="InicioStack" 
        component={HomeStackNavigator} 
        options={{ title: 'Inicio', headerShown: false }} 
      />
      <Tab.Screen 
        name="Ajustes" 
        component={SettingsStackNavigator} 
        options={{ title: 'Ajustes', headerShown: false }}
        />
    </Tab.Navigator>
  );
}

// ----------------------------------------------------------------------
// Navegador Raíz
// ----------------------------------------------------------------------
const RootStack = createNativeStackNavigator();

export default function RootNavigator() {
  const { isAuthenticated } = useAuth();
  const { theme } = useTheme();
  
  return (
    <RootStack.Navigator 
      screenOptions={{ 
        headerShown: false,
        contentStyle: { backgroundColor: theme.colors.background } 
      }}
    >
      {isAuthenticated ? (
        <RootStack.Screen name="MainApp" component={MainTabNavigator} />
      ) : (
        <RootStack.Screen name="AuthFlow" component={AuthNavigator} />
      )}
    </RootStack.Navigator>
  );
}