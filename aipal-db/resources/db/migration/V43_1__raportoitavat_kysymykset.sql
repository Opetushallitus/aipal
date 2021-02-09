ALTER TABLE kysymys ADD COLUMN raportoitava BOOLEAN DEFAULT TRUE;

UPDATE kysymys SET raportoitava = FALSE WHERE vastaustyyppi = 'vapaateksti';
