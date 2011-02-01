;; Copyright (C) 2010 FoAM vzw
;; This program is free software: you can redistribute it and/or modify
;; it under the terms of the GNU Affero General Public License as
;; published by the Free Software Foundation, either version 3 of the
;; License, or (at your option) any later version.
;;
;; This program is distributed in the hope that it will be useful,
;; but WITHOUT ANY WARRANTY; without even the implied warranty of
;; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;; GNU Affero General Public License for more details.
;;
;; You should have received a copy of the GNU Affero General Public License
;; along with this program.  If not, see <http://www.gnu.org/licenses/>.

(ns oak.forms)

(defn modify [what f thing]
  (merge thing {what (f (what thing))}))

(defn discard [l n]
  (cond
   (empty? l) l
   (= 0 n) l
   :else (cons (first l) (discard (rest l) (- n 1)))))

(defn max-cons [o l m]
  (cons o (discard l m)))

(defn gg []
  (let [i (atom 0)]
    (fn [] (swap! i inc))))

(def generate-id (gg))
