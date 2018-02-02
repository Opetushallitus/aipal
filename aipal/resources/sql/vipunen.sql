-- :name hae-vipunen-vastaukset :? :*
SELECT * FROM vipunen_view
WHERE vastausaika BETWEEN :alkupvm::date AND :loppupvm::date
--~ (if (:since params) "AND vastausid > :since")
ORDER BY vastausid ASC LIMIT :sivunpituus;