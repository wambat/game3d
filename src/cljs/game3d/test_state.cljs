(ns game3d.test-state)
(def state {:battlefield 
            {:size {:x 10 :y 10}}
            {:players [{:id 1 :name "Borat"} {:id 2 :name "Clinton"}]}
            {:actors [{:id 1 
                       :type "infantry" 
                       :player-id 1 
                       :position {:x 1 :y 1} 
                       :params {:health 10 :ammo 99}}
                      {:id 2 
                       :type "infantry" 
                       :player-id 2 
                       :position {:x 2 :y 3} 
                       :params {:health 3 :ammo 9}}]}})
