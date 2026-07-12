import { PublicClientApplication, type Configuration } from '@azure/msal-browser';
import { entraApiScope, entraClientId, entraTenantId } from '../config/env';

const msalConfig: Configuration = {
  auth: {
    clientId: entraClientId,
    authority: `https://login.microsoftonline.com/${entraTenantId}`,
    redirectUri: window.location.origin,
  },
  cache: {
    cacheLocation: 'sessionStorage',
  },
};

export const msalInstance = new PublicClientApplication(msalConfig);

export const loginRequest = {
  scopes: entraApiScope ? [entraApiScope] : ['openid', 'profile', 'email'],
};
