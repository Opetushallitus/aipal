ALTER TABLE kysymys ADD CHECK ( vastaustyyppi IN ('asteikko', 'kylla_ei_valinta', 'monivalinta', 'vapaateksti')) ;
