(ns game3d.actors.proto)

(defprotocol IActor 
  (model [this scene callback] "Get model")
  (get-available-actions [this actor-id state] "Get actions that can be called")
  (get-animation-fn-for-action [this action] "Get animation function for certain action")
  )

(defprotocol IMovable
  (move! [this scene actor x y] "Move to position")
  )
