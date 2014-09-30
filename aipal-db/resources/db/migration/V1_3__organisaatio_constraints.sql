ALTER TABLE kysymysryhma ADD CONSTRAINT kysymysryhma_koulutustoimija_FK FOREIGN KEY ( koulutustoimija ) REFERENCES koulutustoimija ( ytunnus ) NOT DEFERRABLE ;
ALTER TABLE kysymysryhma ADD CONSTRAINT kysymysryhma_oppilaitos_FK FOREIGN KEY ( oppilaitos ) REFERENCES oppilaitos ( oppilaitoskoodi ) NOT DEFERRABLE ;
ALTER TABLE kysymysryhma ADD CONSTRAINT kysymysryhma_toimipaikka_FK FOREIGN KEY ( toimipaikka ) REFERENCES toimipaikka ( toimipaikkakoodi ) NOT DEFERRABLE ;

ALTER TABLE kyselypohja ADD CONSTRAINT kyselypohja_koulutustoimija_FK FOREIGN KEY ( koulutustoimija ) REFERENCES koulutustoimija ( ytunnus ) NOT DEFERRABLE ;
ALTER TABLE kyselypohja ADD CONSTRAINT kyselypohja_oppilaitos_FK FOREIGN KEY ( oppilaitos ) REFERENCES oppilaitos ( oppilaitoskoodi ) NOT DEFERRABLE ;
ALTER TABLE kyselypohja ADD CONSTRAINT kyselypohja_toimipaikka_FK FOREIGN KEY ( toimipaikka ) REFERENCES toimipaikka ( toimipaikkakoodi ) NOT DEFERRABLE ;
