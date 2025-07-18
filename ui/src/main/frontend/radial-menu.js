import { LitElement, html, css } from 'lit';

class RadialMenu extends LitElement {
    static properties = {
        shipImage: { type: String },
        highSlots: { type: Array },
        midSlots: { type: Array },
        lowSlots: { type: Array },
        rigSlots: { type: Array }
    };

    constructor() {
        super();
        this.shipImage = '';
        this.highSlots = [];
        this.midSlots = [];
        this.lowSlots = [];
        this.rigSlots = [];
    }

    static styles = css`
        .menu {
            position: relative;
            width: 300px;
            height: 300px;
            border-radius: 50%;
            background: radial-gradient(circle at center, #1f1f1f 0%, #000 100%);
        }

        .center {
            position: absolute;
            width: 100px;
            height: 100px;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            border-radius: 50%;
            object-fit: cover;
            border: 2px solid #444;
        }

        .slot-icon {
            position: absolute;
            width: 32px;
            height: 32px;
            border-radius: 50%;
            object-fit: contain;
            cursor: pointer;
        }
    `;

    render() {
        return html`
      <div class="menu">
        ${this.shipImage
            ? html`<img class="center" src="${this.shipImage}" />`
            : null}

        ${this._renderSlots(this.highSlots, 6, 110)}
        ${this._renderSlots(this.midSlots, 10, 80)}
        ${this._renderSlots(this.lowSlots, 14, 50)}
        ${this._renderSlots(this.rigSlots, 2, 140)}
      </div>
    `;
    }

    _renderSlots(slots, startAngle, radius) {
        const step = 20;
        return slots.map((slot, i) => {
            const angle = (startAngle + i * step) * (Math.PI / 180);
            const x = 150 + Math.cos(angle) * radius - 16;
            const y = 150 + Math.sin(angle) * radius - 16;
            return html`
        <img
          class="slot-icon"
          src="${slot.icon}"
          title="${slot.name}"
          style="left: ${x}px; top: ${y}px;"
          @click=${() =>
                this.dispatchEvent(
                    new CustomEvent('item-click', {
                        detail: slot,
                        bubbles: true,
                        composed: true
                    })
                )}
        />
      `;
        });
    }
}

customElements.define('radial-menu', RadialMenu);
