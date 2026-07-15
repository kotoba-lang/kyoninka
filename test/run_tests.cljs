;; nbb test runner(第一の実行経路 = cljs/nbb)。repo root から:
;;   nbb --classpath "src:test" test/run_tests.cljs
(require '[cljs.test :as t]
         '[kyoninka.progress-test]
         '[kyoninka.errand-test]
         '[kyoninka.dossier-test])

(defmethod t/report [:cljs.test/default :end-run-tests] [m]
  (when-not (t/successful? m)
    (set! (.-exitCode js/process) 1)))

(t/run-tests 'kyoninka.progress-test 'kyoninka.errand-test 'kyoninka.dossier-test)
