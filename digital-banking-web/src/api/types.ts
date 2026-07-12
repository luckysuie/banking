export interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data: T;
  timestamp?: string;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface ErrorResponse {
  timestamp: string;
  status: number;
  errorCode: string;
  message: string;
  path?: string;
  correlationId?: string;
}

export interface Customer {
  id: string;
  customerNumber: string;
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
  city: string;
  province: string;
  country: string;
  status: string;
}

export interface Account {
  id: string;
  accountNumber: string;
  customerId: string;
  accountType: 'CHEQUING' | 'SAVINGS';
  accountStatus: string;
  currency: string;
  currentBalance: number;
  availableBalance: number;
  dailyTransferLimit: number;
}

export interface Beneficiary {
  id: string;
  customerId: string;
  beneficiaryName: string;
  beneficiaryAccountNumber: string;
  bankName: string;
  transitNumber: string;
  institutionNumber: string;
  nickname?: string;
  status: string;
}

export interface Transaction {
  id: string;
  transactionReference: string;
  accountId: string;
  transactionType: 'DEBIT' | 'CREDIT' | 'TRANSFER';
  amount: number;
  currency: string;
  description?: string;
  status: string;
  balanceBefore: number;
  balanceAfter: number;
  createdAt: string;
}

export interface Notification {
  id: string;
  customerId: string;
  type: string;
  message: string;
  status: string;
  read: boolean;
  createdAt: string;
}

export interface Payment {
  id: string;
  paymentReference: string;
  sourceAccountId: string;
  beneficiaryId: string;
  amount: number;
  currency: string;
  status: string;
  idempotencyKey: string;
}
