alter table
   IDM_MENU
add
   IS_FINANCIAL  varchar2(255);

update IDM_MENU
set IS_FINANCIAL = 'Y'
where cd in (
'MNU_GPCASH_F_FUND_DOMESTIC',
'MNU_GPCASH_F_FUND_INHOUSE',
'MNU_GPCASH_F_MASS_FUND_PAYROLL',
'MNU_GPCASH_F_BILL_PAYMENT',
'MNU_GPCASH_F_LIQ_SWEEP_IN',
'MNU_GPCASH_F_LIQ_SWEEP_OUT'
);
commit;

update PRO_SRVC
set dscp = 'SKN'
where cd = 'GPT_FTR_DOM_LLG';
commit;

update PRO_SRVC
set dscp = 'RTGS'
where cd = 'GPT_FTR_DOM_RTGS';
commit;