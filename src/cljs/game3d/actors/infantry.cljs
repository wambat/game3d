(ns game3d.actors.infantry
  (:require [game3d.actors.proto :as p]))

(def animations 
  {:walk [0 100]
   :attack [50 70]})
(defrecord Infantry [params]
  p/IActor
  (model [this scene callback]
    (BABYLON.SceneLoader.ImportMesh. "him" "assets/Dude/" "Dude.babylon" scene  callback)
    )
  (get-available-actions [this actor-id state] 
    [:move :attack]
    )
  (get-animation-fn-for-action [this action]
    (case action
      :walk (fn [scene skeleton]
              (.beginAnimation scene skeleton 0 100 true 1)
              )
      :run (fn [scene skeleton]
              (.beginAnimation scene skeleton 0 100 true 3)
              )
      :idle (fn [scene skeleton]
              (.beginAnimation scene skeleton 0 10 true 3)
              )
      )

    )
  p/IMovable
  (move! [this scene actor x y]
    (.beginAnimation scene (.-skeleton actor) 0 100 true 0.8)
    )
  )
