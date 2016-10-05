using Improbable.Unity.Visualizer;
using Player;
using UnityEngine;

namespace Assets.Gamelogic.Movement
{
    [RequireComponent(typeof(Rigidbody2D))]
    class PlayerMovement: MonoBehaviour
    {
        [Require] private PlayerValues.Reader Player;

        private Vector3 _positionDelta;
        private Rigidbody2D _rigidbody;

        public void OnEnable()
        {
            _rigidbody = GetComponent<Rigidbody2D>();
        }

        public void FixedUpdate()
        {
            MovePlayer();
        }

        private void MovePlayer()
        {
            _positionDelta.x = Input.GetAxisRaw("Horizontal") * Player.Data.speed;
            _positionDelta.y = Input.GetAxisRaw("Vertical") * Player.Data.speed;
            _rigidbody.velocity = _positionDelta;
        }
    }
}
