(ns game3d.actors.infantry
  (:require [game3d.actors.proto :as p]))

(defrecord Infantry [params]
  p/IActor
  (model [this scene callback]
    (BABYLON.SceneLoader.ImportMesh. "" "assets/toad/" "toad.babylon" scene  callback)
    )
  (get-available-actions [this actor-id state] 
    [:move]
    )
  (get-animation-fn-for-action [this action]
    )
  )
