(ns status-im.native-module.core-test
  (:require [cljs.test :refer [deftest is testing]]
            [status-im.native-module.core :as status]))

(deftest identicon-test
  (testing "check if identicon test works"
    (is
     (=
      "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAADIAAAAyCAYAAAAeP4ixAAAAjklEQVR4nOzXsQmAMBQGYRV3cUAdQwd0Gm2sJIWSBI6f+0oR8XjwSKYhhCE0htAYQmMITUzI/PXF49yv0vN12cYWP1L7/ZiJGEJjCE31xvm7bXptv5iJGEJjCE31WasVz1oPQ2gMoWlyuyvpfaN8i5mIITSG0BhCYwiNIeokZiKG0BhCYwiNITR3AAAA//+A3RtWaKqXgQAAAABJRU5ErkJggg=="
      (status/identicon "a")))))


;; this should fail because the keys are wrong on purpose
(deftest deserialise-compressed-keys-test
  (testing "check if given key is decompressed properly"
           (is
            (=
              "0x0247879a52a7fe6eb06ef4adf9c227ff5f19f910e507c94c8908047586d2f5502e"
             (status/deserialise-compressed-key-into-public-key "zQ3shTAten2v9CwyQD1Kc7VXAqNPDcHZAMsfbLHCZEx6nFqk9")))))


;; this should fail because the keys are wrong on purpose
(deftest serialise-public-keys-test
  (testing "check if given key is decompressed properly"
           (is
            (=
             "zQ3shTAten2v9CwyQD1Kc7VXAqNPDcHZAMsfbLHCZEx6nFqk9"
             (status/serialise-public-key-into-compressed-key "0x0247879a52a7fe6eb06ef4adf9c227ff5f19f910e507c94c8908047586d2f5502e")))))
