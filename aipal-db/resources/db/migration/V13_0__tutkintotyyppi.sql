DO
$$
BEGIN
  IF NOT EXISTS (SELECT column_name FROM information_schema.columns
    WHERE table_schema='public' and table_name='tutkintotyyppi' and column_name='tutkintotyyppi')
  THEN
	create table tutkintotyyppi (
	  tutkintotyyppi varchar(25) not null primary key,
	  luotu_kayttaja varchar(80) not null,
	  muutettu_kayttaja varchar(80) not null,
	  luotuaika timestamptz not null,
	  muutettuaika timestamptz not null
	);

	create trigger tutkintotyyppi_update before update on tutkintotyyppi for each row execute procedure update_stamp() ;
	create trigger tutkintotyyppil_insert before insert on tutkintotyyppi for each row execute procedure update_created() ;
	create trigger tutkintotyyppim_insert before insert on tutkintotyyppi for each row execute procedure update_stamp() ;
	create trigger tutkintotyyppi_mu_update before update on tutkintotyyppi for each row execute procedure update_modifier() ;
	create trigger tutkintotyyppi_cu_insert before insert on tutkintotyyppi for each row execute procedure update_creator() ;
	create trigger tutkintotyyppi_mu_insert before insert on tutkintotyyppi for each row execute procedure update_modifier() ;

	insert into tutkintotyyppi (tutkintotyyppi) values ('erikoisammattitutkinto'), ('ammattitutkinto'), ('perustutkinto');

	alter table tutkinto add column tutkintotyyppi varchar(25) references tutkintotyyppi(tutkintotyyppi);
  ELSE
    raise NOTICE 'Already exists';
  END IF;
END
$$
