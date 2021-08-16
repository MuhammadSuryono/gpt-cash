Insert into MT_ERROR_MAPPING(CD, CREATED_BY, CREATED_DT, IS_DELETE, IDX,IS_INACTIVE, NM, NM_ID, IS_ROLLBACK, IS_SYSTEM, IS_ERROR, VERSION) Values ('GPT-0100207', 'SYSTEM', sysdate, 'N', 999, 'N', 'Transaction amount for RTGS Transfer Service must not lower than IDR {0}.', 'Minimum Nominal Transaksi untuk layanan RTGS yang diperbolehkan IDR {0}.', 'Y', 'Y','Y', 0);
commit;

delete SYS_PARAMETER;
commit;

SET DEFINE OFF;
Insert into SYS_PARAMETER
   (CD, CREATED_BY, CREATED_DT, IS_DELETE, IDX, 
    IS_INACTIVE, NM, REGEX, IS_SYSTEM, UPDATED_BY, 
    UPDATED_DT, VALUE, VERSION)
 Values
   ('0016', 'SYSTEM', NULL, 'N', 10, 
    'N', 'Minimum RTGS Amount (IDR)', '^[0-9]+$', 'Y', 'USER123', 
    NULL, '100000000', 3);
Insert into SYS_PARAMETER
   (CD, CREATED_BY, CREATED_DT, IS_DELETE, IDX, 
    IS_INACTIVE, NM, REGEX, IS_SYSTEM, UPDATED_BY, 
    UPDATED_DT, VALUE, VERSION)
 Values
   ('0001', 'SYSTEM', NULL, 'N', 3, 
    'N', 'Local Currency Code', '^[a-zA-Z]+$', 'Y', 'USER1', 
    TO_TIMESTAMP('24/08/2017 17:49:00.661000','DD/MM/YYYY HH24:MI:SS.FF'), 'IDR', 6);
Insert into SYS_PARAMETER
   (CD, CREATED_BY, CREATED_DT, IS_DELETE, IDX, 
    IS_INACTIVE, NM, REGEX, IS_SYSTEM, UPDATED_BY, 
    UPDATED_DT, VALUE, VERSION)
 Values
   ('0002', 'SYSTEM', NULL, 'N', 13, 
    'Y', 'Immediate Type [C=Immediate on Create, R=Immediate on Release]', '[C|R]', 'Y', NULL, 
    NULL, 'R', 0);
Insert into SYS_PARAMETER
   (CD, CREATED_BY, CREATED_DT, IS_DELETE, IDX, 
    IS_INACTIVE, NM, REGEX, IS_SYSTEM, UPDATED_BY, 
    UPDATED_DT, VALUE, VERSION)
 Values
   ('0003', 'SYSTEM', NULL, 'N', 6, 
    'N', 'Maximum Password History', '^[0-9]+$', 'Y', 'USERMAKER', 
    TO_TIMESTAMP('18/09/2017 10:58:22.463000','DD/MM/YYYY HH24:MI:SS.FF'), '3', 3);
Insert into SYS_PARAMETER
   (CD, CREATED_BY, CREATED_DT, IS_DELETE, IDX, 
    IS_INACTIVE, NM, REGEX, IS_SYSTEM, UPDATED_BY, 
    UPDATED_DT, VALUE, VERSION)
 Values
   ('0004', 'SYSTEM', NULL, 'N', 5, 
    'N', 'Password Validity (Days)', '^[0-9]+$', 'Y', 'SYSTEM', 
    TO_TIMESTAMP('30/03/2017 14:09:11.431000','DD/MM/YYYY HH24:MI:SS.FF'), '90', 1);
Insert into SYS_PARAMETER
   (CD, CREATED_BY, CREATED_DT, IS_DELETE, IDX, 
    IS_INACTIVE, NM, REGEX, IS_SYSTEM, UPDATED_BY, 
    UPDATED_DT, VALUE, VERSION)
 Values
   ('0005', 'SYSTEM', NULL, 'N', 7, 
    'N', 'User Idle Day', '^[0-9]+$', 'Y', 'SYSTEM', 
    NULL, '120', 1);
Insert into SYS_PARAMETER
   (CD, CREATED_BY, CREATED_DT, IS_DELETE, IDX, 
    IS_INACTIVE, NM, REGEX, IS_SYSTEM, UPDATED_BY, 
    UPDATED_DT, VALUE, VERSION)
 Values
   ('0006', 'SYSTEM', NULL, 'N', 4, 
    'N', 'Minimum Password Length', '^[0-9]+$', 'Y', 'USERMAKER', 
    TO_TIMESTAMP('18/09/2017 18:56:38.364000','DD/MM/YYYY HH24:MI:SS.FF'), '8', 4);
Insert into SYS_PARAMETER
   (CD, CREATED_BY, CREATED_DT, IS_DELETE, IDX, 
    IS_INACTIVE, NM, REGEX, IS_SYSTEM, UPDATED_BY, 
    UPDATED_DT, VALUE, VERSION)
 Values
   ('0007', 'SYSTEM', NULL, 'N', 8, 
    'N', 'Session Time (HH:MM)', '.+', 'Y', 'USERMAKER', 
    TO_TIMESTAMP('20/09/2017 16:09:35.923000','DD/MM/YYYY HH24:MI:SS.FF'), '07:00|11:00|13:00|16:00', 5);
Insert into SYS_PARAMETER
   (CD, CREATED_BY, CREATED_DT, IS_DELETE, IDX, 
    IS_INACTIVE, NM, REGEX, IS_SYSTEM, UPDATED_BY, 
    UPDATED_DT, VALUE, VERSION)
 Values
   ('0008', 'SYSTEM', NULL, 'N', 2, 
    'N', 'Product Name', '.+', 'Y', 'SYSTEM', 
    NULL, 'CMS', 2);
Insert into SYS_PARAMETER
   (CD, CREATED_BY, CREATED_DT, IS_DELETE, IDX, 
    IS_INACTIVE, NM, REGEX, IS_SYSTEM, UPDATED_BY, 
    UPDATED_DT, VALUE, VERSION)
 Values
   ('0009', 'SYSTEM', NULL, 'N', 12, 
    'Y', 'Default Branch Code', '^[0-9]+$', 'Y', 'SYSTEM', 
    NULL, '0000', 2);
Insert into SYS_PARAMETER
   (CD, CREATED_BY, CREATED_DT, IS_DELETE, IDX, 
    IS_INACTIVE, NM, REGEX, IS_SYSTEM, UPDATED_BY, 
    UPDATED_DT, VALUE, VERSION)
 Values
   ('0010', 'SYSTEM', NULL, 'N', 9, 
    'N', 'RTGS Threshold (IDR)', '^[0-9]+$', 'Y', 'USER123', 
    TO_TIMESTAMP('02/10/2017 18:04:45.300000','DD/MM/YYYY HH24:MI:SS.FF'), '500000000', 3);
Insert into SYS_PARAMETER
   (CD, CREATED_BY, CREATED_DT, IS_DELETE, IDX, 
    IS_INACTIVE, NM, REGEX, IS_SYSTEM, UPDATED_BY, 
    UPDATED_DT, VALUE, VERSION)
 Values
   ('0011', 'SYSTEM', NULL, 'N', 3, 
    'Y', 'Local Country Code', '^[a-zA-Z]+$', 'Y', 'USER1', 
    NULL, 'ID', 6);
Insert into SYS_PARAMETER
   (CD, CREATED_BY, CREATED_DT, IS_DELETE, IDX, 
    IS_INACTIVE, NM, REGEX, IS_SYSTEM, UPDATED_BY, 
    UPDATED_DT, VALUE, VERSION)
 Values
   ('0012', 'SYSTEM', NULL, 'N', 14, 
    'Y', 'Default Account Debit', '^[0-9]+$|^[0-9]+[.]?[0-9]+$', 'Y', 'SYSTEM', 
    TO_TIMESTAMP('13/12/2012 15:18:48.000000','DD/MM/YYYY HH24:MI:SS.FF'), '1000000000', 5);
Insert into SYS_PARAMETER
   (CD, CREATED_BY, CREATED_DT, IS_DELETE, IDX, 
    IS_INACTIVE, NM, REGEX, IS_SYSTEM, UPDATED_BY, 
    UPDATED_DT, VALUE, VERSION)
 Values
   ('0014', 'SYSTEM', NULL, 'N', 1, 
    'N', 'Bank Name', '.+', 'Y', 'USER1', 
    TO_TIMESTAMP('23/08/2017 14:39:59.242000','DD/MM/YYYY HH24:MI:SS.FF'), 'BJB', 5);
Insert into SYS_PARAMETER
   (CD, CREATED_BY, CREATED_DT, IS_DELETE, IDX, 
    IS_INACTIVE, NM, REGEX, IS_SYSTEM, UPDATED_BY, 
    UPDATED_DT, VALUE, VERSION)
 Values
   ('0015', 'SYSTEM', NULL, 'N', 11, 
    'N', 'Account Statement Period (Month)', '^[0-9]+$', 'Y', 'SYSTEM', 
    TO_TIMESTAMP('30/03/2017 14:09:11.431000','DD/MM/YYYY HH24:MI:SS.FF'), '3', 1);
Insert into SYS_PARAMETER
   (CD, CREATED_BY, CREATED_DT, IS_DELETE, IDX, 
    IS_INACTIVE, NM, REGEX, IS_SYSTEM, UPDATED_BY, 
    UPDATED_DT, VALUE, VERSION)
 Values
   ('0013', 'SYSTEM', NULL, 'N', 15, 
    'N', 'Maximum Record Bulk Transaction (Records)', '^[0-9]+$', 'Y', 'SYSTEM', 
    TO_TIMESTAMP('30/03/2017 14:09:11.431000','DD/MM/YYYY HH24:MI:SS.FF'), '5000', 1);
COMMIT;
