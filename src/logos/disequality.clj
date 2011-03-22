(ns logos.disequality
  (:refer-clojure :exclude [reify == inc])
  (:use [logos.minikanren :exclude [==]]
        [clojure.set :only [rename-keys]]
        logos.match)
  (:import [logos.minikanren Substitutions]))

;; all-different ?

(defmacro != [u v]
  `(fn [a#]
     (!=-verify a# (unify a# u v))))

(defmacro == [u v]
  `(fn [a#]
     (!=-verify (unify a# u v) a#)))

(defprotocol IDisequality
  (!=-verify [this sp])
  (==-verify [this u v]))

(defn verify-simple [])

(defn verify-complex [])

(defn constraint [s u v]
  (if-let [meta (meta u)]
    (let [{:keys [simple complex]} meta]
      nil)
    true))

(defn merge-constraints [c1 c2]
  )

(defn prefix [s <s]
  (if (= s <s)
    ()
    (cons (first s) (prefix (rest s) <s))))

(extend-type Substitutions
  IDisequality

  (!=-verify [this sp]
             (let [^Substitutions sp sp]
              (cond
               (not sp) this
               (= this sp) nil
               :else (let [[[k v] & r :as c] (into {} (prefix (.l sp) (.l this)))
                           nc (if (= (count c) 1)
                                {:simple #{v} :complex []}
                                {:simple #{} :complex [c]})
                           ks (keys c)
                           nks (zipmap ks (map #(with-meta %
                                                  (merge-constraints (meta %) nc))
                                               ks))
                           os (.s this)]
                       (Substitutions. (rename-keys os nks)
                                       (.l this) constraint))))))

(comment
  (let [x (lvar 'x)
        y (lvar 'y)
        z (lvar 'z)
        s1 (-> empty-s
                (ext-no-check x 1)
                (ext-no-check y 2))
        s2 (ext-no-check s1 z 3)]
    (prefix (.l s2) (.l s1)))

  ;; plenty fast
  ;; 1s
  (dotimes [_ 10]
    (time
     (dotimes [_ 2e6]
       (let [x (lvar 'x)
             y (lvar 'y)
             z (lvar 'z)
             s1 (-> empty-s
                    (ext-no-check x 1)
                    (ext-no-check y 2))
             s2 (ext-no-check s1 z 3)]
         (prefix (.l ^Substitutions s2) (.l ^Substitutions s1))))))
  )