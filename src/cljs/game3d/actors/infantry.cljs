(ns game3d.actors.infantry
  (:require [game3d.actors.proto :as p]))

(defrecord Infantry [params]
  p/IActor
  (model [this scene callback]
    (BABYLON.SceneLoader.ImportMesh. "him" "assets/Dude/" "Dude.babylon" scene  callback)
    )
  (get-available-actions [this actor-id state] 
    [:move]
    )
  (get-animation-fn-for-action [this action]
    )
  p/IMovable
  (move! [this scene actor x y]
    (.beginAnimation scene (.-skeleton actor) 0 100 true 0.8)
    )
  )
