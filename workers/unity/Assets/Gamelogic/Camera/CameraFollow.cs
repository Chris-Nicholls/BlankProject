using Assets.Gamelogic.Player;
using UnityEngine;

namespace Camera
{
    
    public class CameraFollow : MonoBehaviour
    {
        public float ZOffset;
        private Vector3 _position;

        private void LateUpdate()
        {
            var Target = LocalPlayerCheck.LocalPlayer;
            if (Target == null)
            {
                return;
            }
            _position.x = Target.transform.position.x;
            _position.y = Target.transform.position.y;
            _position.z = Target.transform.position.z + ZOffset;
            transform.position = _position;
        }
    }

}
