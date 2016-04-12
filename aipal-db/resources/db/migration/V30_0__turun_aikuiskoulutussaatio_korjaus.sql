update monivalintavaihtoehto set teksti_fi = 'Erinomainen', teksti_sv = 'Erinomainen'
where jarjestys = 5
and ((monivalintavaihtoehtoid = 457376 and kysymysid = 457361) or
     (monivalintavaihtoehtoid = 932989 and kysymysid = 932974) or
     (monivalintavaihtoehtoid = 938387 and kysymysid = 938372) or
     (monivalintavaihtoehtoid = 2763664 and kysymysid = 2763649) or
     (monivalintavaihtoehtoid = 457192 and kysymysid = 4457181));
