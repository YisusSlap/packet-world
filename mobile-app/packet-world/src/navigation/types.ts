import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import type { BottomTabScreenProps } from '@react-navigation/bottom-tabs';
import type { CompositeScreenProps, NavigatorScreenParams } from '@react-navigation/native';

// --- Auth Stack ---
export type AuthStackParamList = {
  Login: undefined;
  // Register eliminado
};

// --- Home Stack ---
export type HomeStackParamList = {
  Home: undefined;          
  ShipmentsList: undefined; 
  ShipmentDetail: { shipmentId: string };
  EditProfile: undefined;
  ChangePassword: undefined;
};

// --- Tabs Principales ---
export type RootTabParamList = {
  InicioStack: NavigatorScreenParams<HomeStackParamList>; 
  Ajustes: undefined;
};

// --- Helpers para Props de Pantallas ---
export type LoginScreenProps = NativeStackScreenProps<AuthStackParamList, 'Login'>;

export type HomeScreenProps = CompositeScreenProps(
  NativeStackScreenProps<HomeStackParamList, 'Home'>,
  BottomTabScreenProps<RootTabParamList>
);

export type ShipmentsListScreenProps = NativeStackScreenProps<HomeStackParamList, 'ShipmentsList'>;