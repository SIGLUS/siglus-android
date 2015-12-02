module EnvConfig

    STRESS_TEST = false

    def self.getConfig()

        dev_env=false

        if dev_env
            {
                username:"superuser",
                password:"password1",
                mmiaSignature:true,
                stockMovementSignature:true,
                viaSignature:true,
            }
        else
            {
                username:"testuser",
                password:"testuser123",
                mmiaSignature:false,
                stockMovementSignature:false,
                viaSignature:false,
            }
        end
    end
end
